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

package org.dockbox.hartshorn.server.minecraft.item;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.di.context.DefaultContext;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ItemContext extends DefaultContext {

    @Getter
    private final List<String> items;

    @Getter
    private final List<String> blocks;

    private final Map<String, Supplier<Item>> custom = HartshornUtils.emptyConcurrentMap();

    public Item custom(String identifier) {
        return this.custom.getOrDefault(identifier, () -> Item.of(ItemTypes.AIR)).get();
    }

    public ItemContext register(String identifier, Item item) {
        return this.register(identifier, () -> item);
    }

    public ItemContext register(String identifier, Supplier<Item> item) {
        if (this.custom.containsKey(identifier))
            Hartshorn.log().warn("Overwriting custom item identifier '" + identifier + "'");
        this.custom.put(identifier, item);
        return this;
    }

}
