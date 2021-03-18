/*
 * Copyright (C) 2020 Guus Lieben
 *
 * This framework is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.selene.sponge.plotsquared;

import com.plotsquared.sponge.events.PlayerClaimPlotEvent;
import com.plotsquared.sponge.events.PlayerEnterPlotEvent;
import com.plotsquared.sponge.events.PlayerLeavePlotEvent;
import com.plotsquared.sponge.events.PlayerPlotDeniedEvent;
import com.plotsquared.sponge.events.PlayerPlotHelperEvent;
import com.plotsquared.sponge.events.PlayerPlotTrustedEvent;
import com.plotsquared.sponge.events.PlayerTeleportToPlotEvent;
import com.plotsquared.sponge.events.PlotAutoMergeEvent;
import com.plotsquared.sponge.events.PlotChangeOwnerEvent;
import com.plotsquared.sponge.events.PlotClearEvent;
import com.plotsquared.sponge.events.PlotComponentSetEvent;
import com.plotsquared.sponge.events.PlotDeleteEvent;
import com.plotsquared.sponge.events.PlotFlagAddEvent;
import com.plotsquared.sponge.events.PlotFlagRemoveEvent;
import com.plotsquared.sponge.events.PlotUnlinkEvent;

import org.dockbox.selene.api.PlayerStorageService;
import org.dockbox.selene.api.WorldStorageService;
import org.dockbox.selene.api.events.parents.Cancellable;
import org.dockbox.selene.api.objects.location.Direction;
import org.dockbox.selene.api.objects.location.World;
import org.dockbox.selene.plots.Plot;
import org.dockbox.selene.plots.PlotMembership;
import org.dockbox.selene.plots.PlotProperties;
import org.dockbox.selene.plots.events.ClearPlotEvent;
import org.dockbox.selene.plots.events.DeletePlotEvent;
import org.dockbox.selene.plots.events.PlotChangePropertyEvent;
import org.dockbox.selene.plots.events.flags.PlotFlagAddedEvent;
import org.dockbox.selene.plots.events.flags.PlotFlagRemovedEvent;
import org.dockbox.selene.plots.events.membership.ClaimPlotEvent;
import org.dockbox.selene.plots.events.membership.PlotMembershipChanged;
import org.dockbox.selene.plots.events.merge.PlotMergeEvent;
import org.dockbox.selene.plots.events.movement.EnterPlotEvent;
import org.dockbox.selene.plots.events.movement.LeavePlotEvent;
import org.dockbox.selene.plots.events.movement.TeleportToPlotEvent;
import org.dockbox.selene.sponge.util.SpongeConversionUtil;
import org.spongepowered.api.event.Listener;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class PlotSquaredEventListener {

    @Inject
    private PlayerStorageService players;

    @Inject
    private WorldStorageService worlds;

    @Listener
    public void onPlotClaimed(PlayerClaimPlotEvent event) {
        Cancellable cancellable = new ClaimPlotEvent(
                new SpongePlot(event.getPlot()),
                SpongeConversionUtil.fromSponge(event.getPlayer()),
                event.wasAuto()
        ).post();
        event.setCancelled(cancellable.isCancelled());
    }

    @Listener
    public void onPlotEnter(PlayerEnterPlotEvent event) {
        new EnterPlotEvent(new SpongePlot(event.getPlot()), SpongeConversionUtil.fromSponge(event.getPlayer())).post();
    }

    @Listener
    public void onPlotLeave(PlayerLeavePlotEvent event) {
        new LeavePlotEvent(new SpongePlot(event.getPlot()), SpongeConversionUtil.fromSponge(event.getPlayer())).post();
    }

    @Listener
    public void onPlotDenied(PlayerPlotDeniedEvent event) {
        new PlotMembershipChanged(
                new SpongePlot(event.getPlot()),
                players.getPlayer(event.getPlayer()).orNull(),
                SpongeConversionUtil.fromSponge(event.getInitiator()),
                PlotMembership.DENIED
        ).post();
    }

    @Listener
    public void onPlotHelper(PlayerPlotHelperEvent event) {
        new PlotMembershipChanged(
                new SpongePlot(event.getPlot()),
                players.getPlayer(event.getPlayer()).orNull(),
                SpongeConversionUtil.fromSponge(event.getInitiator()),
                PlotMembership.MEMBER
        ).post();
    }

    @Listener
    public void onPlotTrusted(PlayerPlotTrustedEvent event) {
        new PlotMembershipChanged(
                new SpongePlot(event.getPlot()),
                players.getPlayer(event.getPlayer()).orNull(),
                SpongeConversionUtil.fromSponge(event.getInitiator()),
                PlotMembership.TRUSTED
        ).post();
    }

    @Listener
    public void onTeleportToPlot(PlayerTeleportToPlotEvent event) {
        new TeleportToPlotEvent(
                new SpongePlot(event.getPlot()),
                SpongeConversionUtil.fromSponge(event.getPlayer()),
                SpongeConversionUtil.fromPlotSquared(event.getFrom()),
                SpongeConversionUtil.fromPlotSquared(event.getPlot().getHome())
        );
    }

    @Listener
    public void onPlotChangeOwner(PlotChangeOwnerEvent event) {
        new PlotMembershipChanged(
                new SpongePlot(event.getPlot()),
                players.getPlayer(event.getNewOwner()).orNull(),
                SpongeConversionUtil.fromSponge(event.getInitiator()),
                PlotMembership.OWNER
        ).post();
    }

    @Listener
    public void onPlotClear(PlotClearEvent event) {
        worlds.getWorld(event.getWorld()).ifPresent(world -> {
            Cancellable cancellable = new ClearPlotEvent(world, event.getPlotId().x, event.getPlotId().y);
            event.setCancelled(cancellable.isCancelled());
        });
    }

    // E.g. plot wall, floor, border, etc
    @Listener
    public void onPlotComponentChange(PlotComponentSetEvent event) {
        PlotProperties property = null;
        switch (event.getComponent()) {
            case "main":
                property = PlotProperties.FILLING;
                break;
            case "floor":
                property = PlotProperties.FLOOR;
                break;
            case "air":
                property = PlotProperties.AIR;
                break;
            case "all":
                property = PlotProperties.ALL;
                break;
            case "border":
                property = PlotProperties.WALL_BORDER;
                break;
            case "wall":
                property = PlotProperties.WALL_FILLING;
                break;
            case "outline":
                property = PlotProperties.OUTLINE;
                break;
            case "middle":
                property = PlotProperties.MIDDLE;
                break;
        }
        PlotProperties finalProperty = property;
        worlds.getWorld(event.getWorld()).ifPresent(world -> {
            new PlotChangePropertyEvent(
                    Plot.getById(world, event.getPlotId().x, event.getPlotId().y),
                    finalProperty
            ).post();
        });
    }

    @Listener
    public void onPlotDelete(PlotDeleteEvent event) {
        worlds.getWorld(event.getWorld()).ifPresent(world -> {
            Cancellable cancellable = new DeletePlotEvent(world, event.getPlotId().x, event.getPlotId().y);
            event.setCancelled(cancellable.isCancelled());
        });
    }

    @Listener
    public void onPlotFlagAdd(PlotFlagAddEvent event) {
        Cancellable cancellable = new PlotFlagAddedEvent(
                new SpongePlot(event.getPlot()),
                new SpongeFlagWrapper<>(event.getFlag())
        ).post();
        event.setCancelled(cancellable.isCancelled());
    }

    @Listener
    public void onPlotFlagRemove(PlotFlagRemoveEvent event) {
        Cancellable cancellable = new PlotFlagRemovedEvent(
                new SpongePlot(event.getPlot()),
                new SpongeFlagWrapper<>(event.getFlag())
        ).post();
        event.setCancelled(cancellable.isCancelled());
    }

    @Listener
    public void onPlotMerge(com.plotsquared.sponge.events.PlotMergeEvent event) {
        Cancellable cancellable = new PlotMergeEvent(
                new SpongePlot(event.getPlot()),
                Direction.getInstance(event.getDir())
        ).post();
        event.setCancelled(cancellable.isCancelled());
    }

    @Listener
    public void onPlotAutoMerge(PlotAutoMergeEvent event) {
        World world = worlds.getWorld(event.getWorld().getUniqueId()).orNull();
        Collection<Plot> plots = event.getPlots().stream().map(plotId -> Plot.getById(world, plotId.x, plotId.y)).collect(Collectors.toList());
        Cancellable cancellable = new org.dockbox.selene.plots.events.merge.PlotAutoMergeEvent(
                new SpongePlot(event.getPlot()),
                plots
        ).post();
        event.setCancelled(cancellable.isCancelled());

    }

    @Listener
    public void onPlotUnlink(PlotUnlinkEvent event) {
        World world = worlds.getWorld(event.getWorld().getUniqueId()).orNull();
        Collection<Plot> plots = event.getPlots().stream().map(plotId -> Plot.getById(world, plotId.x, plotId.y)).collect(Collectors.toList());
        Cancellable cancellable = new org.dockbox.selene.plots.events.merge.PlotUnlinkEvent(plots).post();
        event.setCancelled(cancellable.isCancelled());
    }

}
