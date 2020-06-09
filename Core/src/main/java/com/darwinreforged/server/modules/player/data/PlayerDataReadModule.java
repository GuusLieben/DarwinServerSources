package com.darwinreforged.server.modules.player.data;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.chat.ClickEvent;
import com.darwinreforged.server.core.chat.ClickEvent.ClickAction;
import com.darwinreforged.server.core.chat.HoverEvent;
import com.darwinreforged.server.core.chat.HoverEvent.HoverAction;
import com.darwinreforged.server.core.chat.Pagination;
import com.darwinreforged.server.core.chat.Text;
import com.darwinreforged.server.core.commands.annotations.Command;
import com.darwinreforged.server.core.commands.annotations.Permission;
import com.darwinreforged.server.core.commands.context.CommandArgument;
import com.darwinreforged.server.core.commands.context.CommandContext;
import com.darwinreforged.server.core.files.FileManager;
import com.darwinreforged.server.core.math.Vector3i;
import com.darwinreforged.server.core.modules.Module;
import com.darwinreforged.server.core.player.DarwinPlayer;
import com.darwinreforged.server.core.player.PlayerManager;
import com.darwinreforged.server.core.resources.Permissions;
import com.darwinreforged.server.core.resources.translations.DefaultTranslations;
import com.darwinreforged.server.core.types.living.CommandSender;
import com.darwinreforged.server.core.types.location.DarwinLocation;
import com.darwinreforged.server.core.types.location.DarwinWorld;
import com.darwinreforged.server.core.util.LocationUtils;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.j256.ormlite.dao.Dao;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Module(id = "playerData", name = "Player Data", description = "Provides commands to obtain information about given players", authors = "GuusLieben")
public class PlayerDataReadModule {

    @Command(aliases = {"oldplots", "olp"}, usage = "oldplots <player>", desc = "Retrieves the list of old plots for a given player", context = "oldplots <player{String}>")
    @Permission(Permissions.OLP_LIST)
    public void getOldPlotList(CommandSender src, CommandContext ctx) {
        Optional<CommandArgument<String>> optionalPlayerArg = ctx.getStringArgument("player");
        if (!optionalPlayerArg.isPresent()) {
            src.sendMessage(DefaultTranslations.ARGUMENT_NOT_PROVIDED.f("player"), false);
            return;
        }

        String playerName = optionalPlayerArg.get().getValue();

        FileManager fm = DarwinServer.getUtilMan().get(FileManager.class);
        Path dataDir = fm.getDataDirectory(this);
        File file = new File(dataDir.toFile(), "oldplots.db");

        if (!file.exists()) {
            src.sendMessage(PlayerDataTranslations.OLP_NO_STORAGE_FILE.s(), false);
            return;
        }

        Dao<PlotStorageModel, Integer> plotStorageModelDao = fm.getDataDb(PlotStorageModel.class, file);

        Map<String, Object> res = fm.getYamlDataForUrl(String.format("https://api.mojang.com/users/profiles/minecraft/%s", playerName));
        if (res.containsKey("id")) {
            Map<String, Object> playerToFind = new HashMap<>();
            UUID playerUuid = UUID.fromString(
                    res.get("id").toString()
                            .replaceFirst(
                                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
                            ));
            playerToFind.put("owner", playerUuid);
            try {
                List<PlotStorageModel> plotStorageModels = plotStorageModelDao.queryForFieldValues(playerToFind);
                List<String> foundPlots = new ArrayList<>();
                List<Text> paginationContent = new ArrayList<>();
                plotStorageModels.forEach(psm -> {
                    String plotLoc = String.format("%s,%s;%s", psm.getWorld(), psm.getPlotIdX(), psm.getPlotIdZ());
                    if (!psm.getWorld().equals("*") && !foundPlots.contains(plotLoc)) {
                        Text singlePlot = Text.of(PlayerDataTranslations.OLP_LIST_ITEM.f(psm.getId(), psm.getWorld(), psm.getPlotIdZ(), psm.getPlotIdX()));
                        if (src instanceof DarwinPlayer) {
                            singlePlot.setClickEvent(new ClickEvent(ClickAction.RUN_COMMAND, String.format("/olptp %s %d %d", psm.getWorld(), psm.getPlotIdX(), psm.getPlotIdZ())));
                            singlePlot.setHoverEvent(new HoverEvent(HoverAction.SHOW_TEXT,
                                    PlayerDataTranslations.OLP_TELEPORT_HOVER.f(psm.getWorld(), psm.getPlotIdX(), psm.getPlotIdZ())));
                        }
                        paginationContent.add(singlePlot);
                        foundPlots.add(plotLoc);
                    }
                });
                Pagination pagination = Pagination.builder().contents(paginationContent).title(Text.of(PlayerDataTranslations.OLP_LIST_HEADER.f(playerName))).build();
                pagination.sendTo(src);
            } catch (SQLException e) {
                DarwinServer.error("Failed to read OldPlots database", e);
                src.sendMessage(PlayerDataTranslations.OLP_FAILED_READ, false);
            }
        } else {
            src.sendMessage(DefaultTranslations.PLAYER_NOT_FOUND.f(playerName), false);
        }

    }

    @Command(aliases = "olptp", usage = "olptp <world> <x> <z>", desc = "Teleports the player to the corner of an OldPlot", context = "olptp <world{string}> <x{integer}> <z{integer}>")
    public void teleportToPlot(DarwinPlayer player, CommandContext ctx) {
        Optional<CommandArgument<Integer>> xArgCandidate = ctx.getIntArgument("x");
        Optional<CommandArgument<Integer>> zArgCandidate = ctx.getIntArgument("z");
        Optional<CommandArgument<String>> worldArgCandidate = ctx.getStringArgument("world");
        if (xArgCandidate.isPresent() && zArgCandidate.isPresent() && worldArgCandidate.isPresent()) {
            String teleportToWorld = worldArgCandidate.get().getValue();
            // Due to several database merges some worlds do not include the OldPlots format, and are named as regular worlds
            if (!teleportToWorld.startsWith("Old")) teleportToWorld = "Old" + teleportToWorld;
            try {
                OldPlotWorld oldPlotWorld = Enum.valueOf(OldPlotWorld.class, teleportToWorld.toUpperCase());
                int teleportToX = oldPlotWorld.getHomeX(xArgCandidate.get().getValue());
                int teleportToZ = oldPlotWorld.getHomeZ(zArgCandidate.get().getValue());

                Optional<DarwinWorld> worldCandidate = DarwinServer.getUtilMan().get(LocationUtils.class).getWorld(teleportToWorld);
                if (worldCandidate.isPresent()) {
                    DarwinWorld world = worldCandidate.get();
                    Vector3i vec3i = new Vector3i(teleportToX, oldPlotWorld.getHeight(), teleportToZ);
                    DarwinLocation location = new DarwinLocation(world, vec3i);
                    player.teleport(location);
                    player.sendMessage(PlayerDataTranslations.OLP_TELEPORTED_TO.f(teleportToWorld, xArgCandidate.get().getValue(), zArgCandidate.get().getValue()), false);
                } else {
                    player.sendMessage(PlayerDataTranslations.OLP_NO_WORLD_PRESENT.f(teleportToWorld), false);
                }

            } catch (IllegalArgumentException e) {
                DarwinServer.error(String.format("Failed to obtain associated OldPlotWorld for value '%s'", teleportToWorld), e);
                player.sendMessage(PlayerDataTranslations.OLP_NOT_ASSOCIATED.f(teleportToWorld), false);
            }
        }
    }

    @Command(aliases = "locate", usage = "locate [player]", desc = "Shows what world a player is in", context = "locate [player{Player}] --plot -p")
    @Permission(Permissions.WW_USE)
    public void locatePlayer(CommandSender src, CommandContext ctx) {
        Optional<CommandArgument<DarwinPlayer>> playerCandidate = ctx.getArgument("player", DarwinPlayer.class);

        if (ctx.hasFlag("p") || ctx.hasFlag("plot")) {
            if (playerCandidate.isPresent()) {
                DarwinPlayer player = playerCandidate.get().getValue();
                PlotPlayer plotPlayer = PlotPlayer.get(player.getName());
                Plot currentPlot = plotPlayer.getCurrentPlot();
                if (currentPlot == null) src.sendMessage(PlayerDataTranslations.PLAYER_ON_ROAD.f(player.getName()), false);
                else
                    src.sendMessage(PlayerDataTranslations.PLAYER_IN_PLOT.f(player.getName(), currentPlot.getWorldName(), currentPlot.getId().toCommaSeparatedString()), false);
            } else src.sendMessage(DefaultTranslations.UNKNOWN_PLAYER.s(), false);

        } else {
            if (playerCandidate.isPresent()) {
                DarwinPlayer p = playerCandidate.get().getValue();
                worldForPlayer(p, src);
            } else DarwinServer.getUtilMan().get(PlayerManager.class).getOnlinePlayers().forEach(p -> worldForPlayer(p, src));
        }
    }

    private void worldForPlayer(DarwinPlayer p, CommandSender src) {
        String wn = p.getWorld().map(DarwinWorld::getName).orElse("Unknown");
        src.sendMessage(PlayerDataTranslations.PLAYER_IN_WORLD.f(p.getName(), wn), false);
    }
}
