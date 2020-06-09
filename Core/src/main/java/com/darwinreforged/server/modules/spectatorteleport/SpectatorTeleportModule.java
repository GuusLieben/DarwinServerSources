package com.darwinreforged.server.modules.spectatorteleport;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.events.internal.player.PlayerTeleportEvent;
import com.darwinreforged.server.core.events.internal.server.ServerReloadEvent;
import com.darwinreforged.server.core.events.internal.server.ServerStartedEvent;
import com.darwinreforged.server.core.events.util.Listener;
import com.darwinreforged.server.core.files.FileManager;
import com.darwinreforged.server.core.modules.Module;
import com.darwinreforged.server.core.player.DarwinPlayer;
import com.darwinreforged.server.core.player.state.GameModes;
import com.darwinreforged.server.core.resources.Permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 The type Spectator teleport module.
 */
@Module(id = "spectatorteleport", name = "Spectator Teleport", description = "Block teleportation for players who are in Spectator mode", authors = {"GuusLieben", "TheCrunchy"})
@SuppressWarnings("unchecked")
public class SpectatorTeleportModule {

    /**
     The Whitelisted worlds.
     */
    List<String> whitelistedWorlds = new ArrayList<>();

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
        Map<String, Object> yamlData = fileManager.getYamlDataForConfig(this);
        if (yamlData.containsKey("whitelist")) {
            whitelistedWorlds = (List<String>) yamlData.get("whitelist");
        } else {
            yamlData = new HashMap<>();
            yamlData.put("whitelist", Arrays.asList("SampleWorld", "Another_World"));
            fileManager.writeYamlDataForConfig(yamlData, this);
        }
    }

    /**
     On teleport.

     @param event
     the event
     */
    @Listener
    public void onTeleport(PlayerTeleportEvent event) {
        DarwinPlayer player = (DarwinPlayer) event.getTarget();
        if (player.getGameMode().equals(GameModes.SPECTATOR)) {
            player.getWorld().ifPresent(world -> {
                if (!whitelistedWorlds.contains(world.getName()) && player.hasPermission(Permissions.GM3TP_IGNORE)) {
                    event.setCancelled(true);
                    player.sendMessage(SpectatorTeleportTranslations.SPECTATOR_TP_DISALLOWED.s(), false);
                }
            });
        }
    }

}
