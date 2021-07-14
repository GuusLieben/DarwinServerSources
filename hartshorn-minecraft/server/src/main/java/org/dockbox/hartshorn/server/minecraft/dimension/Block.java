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

package org.dockbox.hartshorn.server.minecraft.dimension;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.keys.PersistentDataHolder;
import org.dockbox.hartshorn.server.minecraft.dimension.position.Location;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.item.ItemTypes;

import java.util.Map;

public interface Block extends PersistentDataHolder {

    Exceptional<Item> item();
    String id();

    Map<String, Object> states();
    <T> Exceptional<T> state(String state);
    void state(String state, Object value);

    boolean isEmpty();

    boolean place(Location location);

    static Block from(Location location) {
        return Hartshorn.context().get(Block.class, location);
    }

    static Block from(Item item) {
        return Hartshorn.context().get(Block.class, item);
    }

    static Block of(String id) {
        return from(Item.of(id));
    }

    static Block empty() {
        return Hartshorn.context().get(Block.class, Item.of(ItemTypes.AIR));
    }

}
