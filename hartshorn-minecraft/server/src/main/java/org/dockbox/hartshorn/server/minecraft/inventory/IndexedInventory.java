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

package org.dockbox.hartshorn.server.minecraft.inventory;

import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.item.ItemTypes;

public interface IndexedInventory extends Inventory {

    /**
     * Sets the item at the given position within the inventory to the given {@link Item}. If the
     * position is out of bounds, nothing happens. Indices start at zero.
     *
     * @param item
     *         The item to place
     * @param index
     *         The inventory index
     */
    void slot(Item item, int index);

    /**
     * Gets the {@link Item} in the requested position within the inventory. If the position is out of
     * bounds, or if there is no item present, {@link ItemTypes#AIR} is returned instead.
     * Indices start at zero.
     *
     * @param index
     *         The inventory index
     *
     * @return The {@link Item}, or {@link ItemTypes#AIR air}.
     */
    Item slot(int index);

    /**
     * Returns the first occurring index of the given {@link Item}. If the item is not present, -1 is
     * returned.
     *
     * @param item
     *         The item
     *
     * @return The first index of the item, or -1
     */
    default int indexOf(Item item) {
        int capacity = this.capacity() - 1; // -1 to correct for index offset
        while (0 <= capacity) {
            Item slot = this.slot(capacity);
            if (slot.equals(item)) return capacity;
            capacity--;
        }
        return -1; // No matches
    }

}
