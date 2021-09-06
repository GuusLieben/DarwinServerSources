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

package org.dockbox.hartshorn.regions;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.api.domain.tuple.Vector3N;
import org.dockbox.hartshorn.commands.annotations.Command;
import org.dockbox.hartshorn.commands.context.CommandContext;
import org.dockbox.hartshorn.di.annotations.service.Service;
import org.dockbox.hartshorn.i18n.entry.Resource;
import org.dockbox.hartshorn.i18n.text.Text;
import org.dockbox.hartshorn.regions.flags.BooleanFlag;
import org.dockbox.hartshorn.regions.flags.IntegerFlag;
import org.dockbox.hartshorn.regions.persistence.CustomRegion;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.item.ItemTypes;
import org.dockbox.hartshorn.server.minecraft.players.Hand;
import org.dockbox.hartshorn.server.minecraft.players.Player;
import org.dockbox.hartshorn.toolbinding.ToolBinding;

import java.util.UUID;

// TODO GLieben, #342: Remove before PR is approved
@Service
public class DemoService {

    @Command("regions")
    public void regions(final CommandContext context) {
        final DefaultRegionService regions = Hartshorn.context().get(DefaultRegionService.class);
        final CustomRegion region = new CustomRegion(Text.of("$1Demo"),
                Vector3N.of(1, 2, 3),
                Vector3N.of(4, 5, 6),
                UUID.randomUUID(),
                UUID.randomUUID());
        region.add(new IntegerFlag("int_flag", new Resource("Integer flag", "iflag")), 12);
        region.add(new BooleanFlag("bool_flag", new Resource("Boolean flag", "bflag")), true);
        regions.add(region);
    }

    @Command("rwand")
    public void wand(final Player player, final CommandContext context) {
        final Item item = Item.of(ItemTypes.WOODEN_HOE);
        final RegionService service = Hartshorn.context().get(RegionService.class);
        item.set(ToolBinding.TOOL, service.tool());
        player.itemInHand(Hand.MAIN_HAND, item);
        player.inventory().give(item);
    }
}
