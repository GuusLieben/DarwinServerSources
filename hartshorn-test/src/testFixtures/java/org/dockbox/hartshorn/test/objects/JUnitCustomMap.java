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

package org.dockbox.hartshorn.test.objects;

import org.dockbox.hartshorn.api.domain.Identifiable;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.item.ItemTypes;
import org.dockbox.hartshorn.server.minecraft.item.maps.CustomMap;

import lombok.Getter;

public class JUnitCustomMap extends JUnitItem implements CustomMap {

    @Getter
    private final Identifiable owner;
    private final int id;

    public JUnitCustomMap(Identifiable owner, int id) {
        super(Item.of(ItemTypes.FILLED_MAP).id());
        this.owner = owner;
        this.id = id;
    }

    @Override
    public int mapId() {
        return this.id;
    }
}
