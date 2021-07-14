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

import org.dockbox.hartshorn.server.minecraft.dimension.Block;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.item.ItemTypes;

import java.util.Collection;
import java.util.function.Predicate;

/** Represents an inventory UI */
public interface Inventory {

    /**
     * Returns {@code true} if the inventory contains the given {@link Item}, otherwise {@code false}.
     *
     * @param item
     *         The item
     *
     * @return {@code true} if the inventory contains the item, otherwise {@code false}
     */
    default boolean contains(Item item) {
        return 0 < this.count(item);
    }

    /**
     * Returns the total amount of occurrences of the {@link Item} as the total quantity.
     *
     * <p>E.g. if two stacks (64) of {@code minecraft:stone} are inside the inventory, 128 will be
     * returned.
     *
     * @param item
     *         The item
     *
     * @return The total quantity of the item
     */
    default int count(Item item) {
        return this.items().stream()
                .filter(inventoryItem -> inventoryItem.equals(item))
                .mapToInt(Item::amount)
                .sum();
    }

    /**
     * Returns all {@link Item items}, including {@link ItemTypes#AIR}.
     *
     * @return All items inside the inventory.
     */
    Collection<Item> items();

    /**
     * Returns all {@link Item items} which match a given filter. If no items are present, or none
     * match the filter, a empty list is returned.
     *
     * @param filter
     *         The filter
     *
     * @return All items which match the given filter.
     */
    default Collection<Item> findMatching(Predicate<Item> filter) {
        return this.items().stream().filter(filter).toList();
    }

    /**
     * Attempts to give the {@link Item} to the inventory. If the item cannot be added, false is
     * returned.
     *
     * @param item
     *         The item to add
     *
     * @return {@code true} if the item was added, otherwise {@code false}
     */
    boolean give(Item item);

    default boolean give(Block block) {
        return block.item().map(this::give).or(false);
    }

    /**
     * Returns the total capacity of the inventory. This is equal to the maximum index, plus one (to
     * correct for starting at 0).
     *
     * @return The capacity
     */
    int capacity();

}
