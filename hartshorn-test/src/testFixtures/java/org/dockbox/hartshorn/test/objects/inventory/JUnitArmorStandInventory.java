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

import org.dockbox.hartshorn.server.minecraft.entities.ArmorStandInventory;
import org.dockbox.hartshorn.server.minecraft.inventory.Slot;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.item.ItemTypes;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.util.Collection;
import java.util.Map;

public class JUnitArmorStandInventory implements ArmorStandInventory {

    private final Map<Slot, Item> items = HartshornUtils.emptyMap();

    @Override
    public Collection<Item> items() {
        return HartshornUtils.asUnmodifiableList(this.items.values());
    }

    @Override
    public boolean give(Item item) {
        for (Slot slot : Slot.values()) {
            if (!this.items.containsKey(slot)) {
                this.slot(item, slot);
                return true;
            }
        }
        return false;
    }

    @Override
    public Item slot(Slot slot) {
        return this.items.getOrDefault(slot, Item.of(ItemTypes.AIR));
    }

    @Override
    public void slot(Item item, Slot slot) {
        this.items.put(slot, item);
    }
}
