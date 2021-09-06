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

package org.dockbox.hartshorn.regions.persistence;

import org.dockbox.hartshorn.api.domain.tuple.Vector3N;
import org.dockbox.hartshorn.i18n.text.Text;
import org.dockbox.hartshorn.persistence.PersistentModel;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
public class PersistentRegion implements PersistentModel<CustomRegion> {

    @Id @Getter
    private long id;

    private String name;
    private String owner;
    private String world;

    private int corner_a_x;
    private int corner_a_y;
    private int corner_a_z;

    private int corner_b_x;
    private int corner_b_y;
    private int corner_b_z;

    @OneToMany(cascade = CascadeType.ALL)
    @Getter private final List<PersistentRegionFlag> flags = HartshornUtils.emptyList();

    public PersistentRegion(
            final long id, final String name, final String owner, final String world,
            final int corner_a_x, final int corner_a_y, final int corner_a_z,
            final int corner_b_x, final int corner_b_y, final int corner_b_z
    ) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.world = world;
        this.corner_a_x = corner_a_x;
        this.corner_a_y = corner_a_y;
        this.corner_a_z = corner_a_z;
        this.corner_b_x = corner_b_x;
        this.corner_b_y = corner_b_y;
        this.corner_b_z = corner_b_z;
    }

    public void add(final PersistentRegionFlag flag) {
        this.flags.add(flag);
    }

    @Override
    public Class<? extends CustomRegion> type() {
        return CustomRegion.class;
    }

    @Override
    public CustomRegion restore() {

        // TODO: Restore flags!
        return new CustomRegion(this.id, Text.of(this.name),
                Vector3N.of(this.corner_a_x, this.corner_a_y, this.corner_a_z),
                Vector3N.of(this.corner_b_x, this.corner_b_y, this.corner_b_z),
                UUID.fromString(this.owner), UUID.fromString(this.world));
    }
}
