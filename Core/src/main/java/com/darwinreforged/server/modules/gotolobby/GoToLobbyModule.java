package com.darwinreforged.server.modules.gotolobby;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.events.internal.player.PlayerMoveEvent;
import com.darwinreforged.server.core.events.internal.server.ServerReloadEvent;
import com.darwinreforged.server.core.events.internal.server.ServerStartedEvent;
import com.darwinreforged.server.core.events.util.Listener;
import com.darwinreforged.server.core.files.FileManager;
import com.darwinreforged.server.core.modules.Module;
import com.darwinreforged.server.core.player.DarwinPlayer;
import com.darwinreforged.server.core.player.state.GameModes;
import com.darwinreforged.server.core.resources.Permissions;
import com.darwinreforged.server.core.types.time.TimeDifference;
import com.darwinreforged.server.core.util.CommonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 The type Go to lobby module.
 */
@Module(id = "gotolobby", name = "GoToLobby", description = "Send players to the lobby if they are in a disallowed world", authors = "GuusLieben")
@SuppressWarnings("unchecked")
public class GoToLobbyModule {

    /**
     The Blacklist.
     */
    List<String> blacklist = new ArrayList<>();

    /**
     On server reload.

     @param event
     the event
     */
    @Listener
    public void onServerReload(ServerReloadEvent event) {
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
        FileManager fileManager = DarwinServer.get(FileManager.class);
        Map<String, Object> config = fileManager.getYamlDataForConfig(this);

        if(config.containsKey("blacklist")) blacklist = (List<String>) config.get("blacklist");
        else config.put("blacklist", Arrays.asList("denied_world", "worlds"));
        fileManager.writeYamlDataForConfig(config, this);
    }

    /**
     On player move.

     @param event
     the event
     */
    @Listener
    public void onPlayerMove(PlayerMoveEvent event) {
        DarwinPlayer player = (DarwinPlayer) event.getTarget();
        Optional<TimeDifference> diff = CommonUtils.getTimeSinceLastUuidTimeout(player.getUniqueId(), this);
        if ((!diff.isPresent()) || diff.get().getSeconds() > 10) {
            CommonUtils.unregisterUuidTimeout(player.getUniqueId(), this);
            player.getWorld().ifPresent(world -> {
                if (blacklist.contains(world.getName()) && !player.hasPermission(Permissions.GTL_IGNORE)) {
                    player.setGameMode(GameModes.CREATIVE);
                    player.sendMessage(GoToLobbyTranslations.GTL_WARPED, false);
                    player.execute("hub");
                    CommonUtils.registerUuidTimeout(player.getUniqueId(), this, false);
                }
            });
        }
    }

}
