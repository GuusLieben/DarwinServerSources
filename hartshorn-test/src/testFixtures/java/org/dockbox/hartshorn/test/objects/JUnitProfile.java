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

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.di.annotations.inject.Bound;
import org.dockbox.hartshorn.server.minecraft.players.Profile;
import org.dockbox.hartshorn.server.minecraft.players.ProfileProperty;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

public class JUnitProfile implements Profile {

    @Getter @Setter private UUID uniqueId;
    @Getter private Set<ProfileProperty> properties;

    @Bound
    public JUnitProfile(final Profile profile) {
        this(profile.uniqueId());
        if (profile instanceof JUnitProfile) this.properties = HartshornUtils.asSet(((JUnitProfile) profile).properties);
        else Hartshorn.log().warn("Could not copy profile properties as the provided profile is not an instance of JUnitProfile");
    }

    @Bound
    public JUnitProfile(final UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public Profile property(final ProfileProperty property) {
        this.properties.add(property);
        return this;
    }

    @Override
    public JUnitProfile properties(final Set<ProfileProperty> properties) {
        this.properties.addAll(properties);
        return this;
    }
}
