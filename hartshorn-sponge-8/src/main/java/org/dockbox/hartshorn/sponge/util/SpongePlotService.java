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

package org.dockbox.hartshorn.sponge.util;

import org.dockbox.hartshorn.di.annotations.inject.Binds;
import org.dockbox.hartshorn.regions.DefaultRegionService;
import org.dockbox.hartshorn.regions.RegionService;
import org.dockbox.hartshorn.regions.flags.RegionFlag;
import org.dockbox.hartshorn.regions.plots.Plot;
import org.dockbox.hartshorn.regions.plots.PlotService;
import org.dockbox.hartshorn.server.minecraft.item.Item;

/**
 * Placeholder implementation of PlotService
 */
@Binds(RegionService.class)
@Binds(PlotService.class)
public class SpongePlotService extends DefaultRegionService implements PlotService {

    @Override
    public void register(final RegionFlag<?> flag) {
        // Nothing happens
    }

    @Override
    public void filling(final Plot plot, final Item item) {
        // Nothing happens
    }

    @Override
    public void floor(final Plot plot, final Item item) {
        // Nothing happens
    }

    @Override
    public void air(final Plot plot, final Item item) {
        // Nothing happens
    }

    @Override
    public void all(final Plot plot, final Item item) {
        // Nothing happens
    }

    @Override
    public void wallBorder(final Plot plot, final Item item) {
        // Nothing happens
    }

    @Override
    public void wallFilling(final Plot plot, final Item item) {
        // Nothing happens
    }

    @Override
    public void outline(final Plot plot, final Item item) {
        // Nothing happens
    }

    @Override
    public void middle(final Plot plot, final Item item) {
        // Nothing happens
    }
}
