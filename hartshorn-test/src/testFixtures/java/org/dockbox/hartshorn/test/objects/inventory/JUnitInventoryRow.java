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

package org.dockbox.hartshorn.test.objects.inventory;

import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.item.ItemTypes;
import org.dockbox.hartshorn.server.minecraft.players.inventory.AbstractInventoryRow;
import org.dockbox.hartshorn.server.minecraft.players.inventory.PlayerInventory;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.util.Collection;
import java.util.Map;

public class JUnitInventoryRow extends AbstractInventoryRow {

    private final Map<Integer, Item> slots = HartshornUtils.emptyMap();

    public JUnitInventoryRow(int rowIndex, PlayerInventory inventory) {
        super(rowIndex, inventory);
    }

    @Override
    public void slot(Item item, int index) {
        if (index < this.capacity()) this.slots.put(index, item);
    }

    @Override
    public Collection<Item> items() {
        return HartshornUtils.asUnmodifiableList(this.slots.values());
    }

    @Override
    public boolean give(Item item) {
        for (int i = 0; i < this.capacity(); i++) {
            if (this.slot(i).isAir()) {
                this.slot(item, i);
                return true;
            }
        }
        return false;
    }

    @Override
    public Item slot(int index) {
        return this.slots.getOrDefault(index, Item.of(ItemTypes.AIR));
    }
}
