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

import org.dockbox.hartshorn.api.domain.tuple.Vector3N;
import org.dockbox.hartshorn.i18n.text.Text;
import org.dockbox.hartshorn.regions.persistence.CustomRegion;
import org.dockbox.hartshorn.server.minecraft.dimension.position.Location;
import org.dockbox.hartshorn.server.minecraft.dimension.world.World;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.util.Map;
import java.util.UUID;

public class WorldRegion extends CustomRegion implements DefaultMembershipRegion {

    private Map<UUID, RegionMembership> memberships = HartshornUtils.emptyMap();

    public WorldRegion(UUID owner, World world) {
        super(
                Text.of(world.name()),
                Vector3N.empty(),
                Vector3N.empty(),
                owner,
                world.worldUniqueId()
        );
    }

    @Override
    public Location center() {
        return Location.of(this.world());
    }

    @Override
    public Vector3N size() {
        return Vector3N.empty();
    }

    @Override
    public Map<UUID, RegionMembership> memberships() {
        return this.memberships;
    }
}
