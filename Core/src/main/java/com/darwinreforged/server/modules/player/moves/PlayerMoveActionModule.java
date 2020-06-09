package com.darwinreforged.server.modules.player.moves;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.events.internal.player.PlayerMoveEvent;
import com.darwinreforged.server.core.events.internal.player.PlayerTeleportEvent;
import com.darwinreforged.server.core.events.internal.server.ServerReloadEvent;
import com.darwinreforged.server.core.events.internal.server.ServerStartedEvent;
import com.darwinreforged.server.core.events.util.Listener;
import com.darwinreforged.server.core.files.FileManager;
import com.darwinreforged.server.core.modules.Module;
import com.darwinreforged.server.core.player.DarwinPlayer;
import com.darwinreforged.server.core.player.state.GameModes;
import com.darwinreforged.server.core.resources.Permissions;
import com.darwinreforged.server.core.types.living.Console;
import com.darwinreforged.server.core.types.location.DarwinLocation;
import com.darwinreforged.server.core.types.time.TimeDifference;
import com.darwinreforged.server.core.util.CommonUtils;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Module(id = "playerMoveActions", name = "Player Move Actions", description = "Performs various checks on player movement in regards to the current world, plot, and gamemode of the player", authors = "GuusLieben")
@SuppressWarnings("unchecked")
public class PlayerMoveActionModule {

    List<String> deniedWorlds = new ArrayList<>();
    List<String> spectatorWhitelist = new ArrayList<>();

    @Listener
    public void onServerReload(ServerReloadEvent event) {
        init();
    }

    @Listener
    public void onServerStart(ServerStartedEvent event) {
        init();
    }

    private void init() {
        FileManager fileManager = DarwinServer.getUtilMan().get(FileManager.class);
        Map<String, Object> config = fileManager.getYamlDataForConfig(this);

        if (config.containsKey("denied_worlds")) deniedWorlds = (List<String>) config.get("denied_worlds");
        else config.put("denied_worlds", Arrays.asList("denied_world", "worlds"));

        if (config.containsKey("spectator_whitelist"))
            spectatorWhitelist = (List<String>) config.get("spectator_whitelist");
        else config.put("spectator_whitelist", Arrays.asList("SampleWorld", "Another_World"));

        fileManager.writeYamlDataForConfig(config, this);
    }

    @Listener
    public void checkGamemodeOnTeleport(PlayerTeleportEvent event) {
        DarwinPlayer player = (DarwinPlayer) event.getTarget();
        if (player.getGameMode().equals(GameModes.SPECTATOR)) {
            player.getWorld().ifPresent(world -> {
                if (!spectatorWhitelist.contains(world.getName()) && player.hasPermission(Permissions.GM3TP_IGNORE)) {
                    event.setCancelled(true);
                    player.sendMessage(PlayerMoveActionTranslations.SPECTATOR_TP_DISALLOWED.s(), false);
                }
            });
        }
    }

    @Listener
    public void checkWorldAllowedOnMove(PlayerMoveEvent event) {
        DarwinPlayer player = (DarwinPlayer) event.getTarget();
        Optional<TimeDifference> diff = CommonUtils.getTimeSinceLastUuidTimeout(player.getUniqueId(), this);
        if ((!diff.isPresent()) || diff.get().getSeconds() > 10) {
            CommonUtils.unregisterUuidTimeout(player.getUniqueId(), this);
            player.getWorld().ifPresent(world -> {
                if (deniedWorlds.contains(world.getName()) && !player.hasPermission(Permissions.GTL_IGNORE)) {
                    player.setGameMode(GameModes.CREATIVE);
                    player.sendMessage(PlayerMoveActionTranslations.GTL_WARPED, false);
                    player.execute("hub");
                    CommonUtils.registerUuidTimeout(player.getUniqueId(), this, false);
                }
            });
        }
    }

    @Listener
    public void checkPlotAllowedOnMove(PlayerTeleportEvent event) {
        if (event.getTarget() instanceof DarwinPlayer) {
            DarwinLocation location = event.getNewLocation();
            boolean cancel = checkPlotAllowed((DarwinPlayer) event.getTarget(), location);
            if (cancel) {
                event.setCancelled(true);
                ((DarwinPlayer) event.getTarget()).sendMessage(PlayerMoveActionTranslations.WD_NOT_PERMITTED, false);
            }
        }
    }

    @Listener
    public void checkPlotAllowedOnMove(PlayerMoveEvent event) {
        if (event.getTarget() instanceof DarwinPlayer) {
            Optional<DarwinLocation> locationCandidate = ((DarwinPlayer) event.getTarget()).getLocation();
            if (locationCandidate.isPresent()) {
                boolean cancel = checkPlotAllowed((DarwinPlayer) event.getTarget(), locationCandidate.get());
                if (cancel) {
                    event.setCancelled(true);
                    ((DarwinPlayer) event.getTarget()).sendMessage(PlayerMoveActionTranslations.WD_NOT_PERMITTED, false);
                }
            }
        }
    }

    private boolean checkPlotAllowed(DarwinPlayer player, DarwinLocation newLocation) {
        Location plotLoc = new Location(newLocation.getWorld().getName(), newLocation.getX().intValue(), newLocation.getY().intValue(), newLocation.getZ().intValue());
        Plot plot = Plot.getPlot(plotLoc);
        if (plot != null) {
            if (plot.isDenied(player.getUniqueId()) && !player.hasPermission(Permissions.ADMIN_BYPASS) && plot.getWorldName().matches("[0-9]+,[0-9]+")) {
                if (player.getGameMode().equals(GameModes.SPECTATOR))
                    Console.instance.execute(String.format("/warn %s Abusing Spectator mode to enter a world you are denied from.", player.getName()));

                player.getLocation().ifPresent(loc -> {
                    if (loc.getWorld().getWorldUUID().equals(newLocation.getWorld().getWorldUUID()))
                        player.execute("/lobby");
                });
                return true;
            }
        }
        return false;
    }

}
