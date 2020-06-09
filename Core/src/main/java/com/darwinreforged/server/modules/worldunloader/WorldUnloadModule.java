package com.darwinreforged.server.modules.worldunloader;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.commands.annotations.Command;
import com.darwinreforged.server.core.commands.annotations.Permission;
import com.darwinreforged.server.core.commands.annotations.Source;
import com.darwinreforged.server.core.commands.context.CommandArgument;
import com.darwinreforged.server.core.commands.context.CommandContext;
import com.darwinreforged.server.core.events.internal.server.ServerReloadEvent;
import com.darwinreforged.server.core.events.internal.server.ServerStartedEvent;
import com.darwinreforged.server.core.events.util.Listener;
import com.darwinreforged.server.core.external.PlotSquaredUtils;
import com.darwinreforged.server.core.files.FileManager;
import com.darwinreforged.server.core.modules.Module;
import com.darwinreforged.server.core.player.DarwinPlayer;
import com.darwinreforged.server.core.resources.Dependencies;
import com.darwinreforged.server.core.resources.Permissions;
import com.darwinreforged.server.core.types.location.DarwinWorld;
import com.darwinreforged.server.core.util.LocationUtils;
import com.darwinreforged.server.core.util.CommonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 The type World unload module.
 */
@SuppressWarnings("unchecked")
@Module(id = "worldunloader", name = "WorldUnloader", description = "Unload worlds with no players in them", authors = "GuusLieben", dependencies = Dependencies.PLOTSQUARED)
public class WorldUnloadModule {

    private FileManager fileUtil;
    private final List<String> unloadBlacklist = new ArrayList<>();

    /**
     On reload.

     @param event
     the event
     */
    @Listener
    public void onReload(ServerReloadEvent event) {
        init();
    }

    /**
     On server start.

     @param event
     the event
     */
    @Listener
    public void onServerStart(ServerStartedEvent event) {
        init();
    }

    private void init() {
        fileUtil = DarwinServer.get(FileManager.class);
        ArrayList<String> blacklist = (ArrayList<String>) fileUtil.getYamlDataForConfig(this, "blacklist", ArrayList.class, new ArrayList<String>());
        if (blacklist != null) unloadBlacklist.addAll(blacklist);
        refreshBlackList();

        // Do not use async, certain platforms do not allow async chunk modifications
        DarwinServer.get(CommonUtils.class).scheduler()
                .interval(2, TimeUnit.MINUTES)
                .execute(this::unloadTask)
                .submit();
    }

    /**
     Add world command.

     @param player
     the player
     @param context
     the context
     @param world
     the world
     */
    @Command(
            aliases = "wu",
            desc = "Registers the given world to be blacklisted from unloading",
            max = 1,
            args = "world",
            usage = "wu [world]",
            context = "wu [world:World]")
    @Permission(Permissions.WU_ADD)
    public void addWorldCommand(DarwinPlayer player, CommandContext context, @Source DarwinWorld world) {
        String worldName;
        if (context.getArgumentCount() == 0) worldName = world.getName();
        else {
            Optional<CommandArgument<DarwinWorld>> worldCandidate = context.getArgument("world", DarwinWorld.class);
            if (worldCandidate.isPresent()) worldName = worldCandidate.get().getValue().getName();
            else {
                player.sendMessage(WorldUnloaderTranslations.WORLD_NOT_FOUND.s(), false);
                return;
            }
        }
        unloadBlacklist.add(worldName);
        refreshBlackList();
        player.sendMessage(WorldUnloaderTranslations.WU_ADDED.f(world), false);
    }

    private void refreshBlackList() {
        Map<String, Object> configData = new HashMap<>();
        configData.put("blacklist", unloadBlacklist.toArray());
        fileUtil.writeYamlDataForConfig(configData, this);
    }

    private void unloadTask() {
        DarwinServer.get(LocationUtils.class)
                .getEmptyWorlds().stream()
                .filter(world -> !unloadBlacklist.contains(world.getName()) && !PlotSquaredUtils.isPlotWorld(world))
                .limit(10).forEach(darwinWorld -> darwinWorld.unloadWorld(false));
    }
}
