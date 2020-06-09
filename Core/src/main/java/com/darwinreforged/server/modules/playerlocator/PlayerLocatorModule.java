package com.darwinreforged.server.modules.playerlocator;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.commands.annotations.Command;
import com.darwinreforged.server.core.commands.annotations.Permission;
import com.darwinreforged.server.core.commands.context.CommandArgument;
import com.darwinreforged.server.core.commands.context.CommandContext;
import com.darwinreforged.server.core.modules.Module;
import com.darwinreforged.server.core.player.DarwinPlayer;
import com.darwinreforged.server.core.player.PlayerManager;
import com.darwinreforged.server.core.resources.Permissions;
import com.darwinreforged.server.core.resources.translations.DefaultTranslations;
import com.darwinreforged.server.core.types.living.CommandSender;
import com.darwinreforged.server.core.types.location.DarwinWorld;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;

import java.util.Optional;

@Module(id = "playerlocator", name = "Player Locator", description = "Shows what world or plot a player is in", authors = {"GuusLieben", "TheCrunchy"})
public class PlayerLocatorModule {

    @Command(aliases = "locate", usage = "locate [player]", desc = "Shows what world a player is in", context = "locate [player{Player}] --plot -p")
    @Permission(Permissions.WW_USE)
    public void locatePlayer(CommandSender src, CommandContext ctx) {
        Optional<CommandArgument<DarwinPlayer>> playerCandidate = ctx.getArgument("player", DarwinPlayer.class);

        if (ctx.hasFlag("p") || ctx.hasFlag("plot")) {
            if (playerCandidate.isPresent()) {
                DarwinPlayer player = playerCandidate.get().getValue();
                PlotPlayer plotPlayer = PlotPlayer.get(player.getName());
                Plot currentPlot = plotPlayer.getCurrentPlot();
                if (currentPlot == null) src.sendMessage(PlayerLocatorTranslations.PLAYER_ON_ROAD.f(player.getName()), false);
                else
                    src.sendMessage(PlayerLocatorTranslations.PLAYER_IN_PLOT.f(player.getName(), currentPlot.getWorldName(), currentPlot.getId().toCommaSeparatedString()), false);
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
        src.sendMessage(PlayerLocatorTranslations.PLAYER_IN_WORLD.f(p.getName(), wn), false);
    }

}
