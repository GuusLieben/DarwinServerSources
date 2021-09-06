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

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.regions.RegionService;
import org.dockbox.hartshorn.regions.flags.RegionFlag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SerializedFlag {

    private transient RegionFlag<?> flag;
    private String id;
    private String value;

    public <T> SerializedFlag(RegionFlag<T> flag, T value) {
        this.flag = flag;
        this.id = flag.id();
        this.value = flag.serialize(value);
    }

    public <T> Exceptional<T> restoreValue() {
        //noinspection unchecked
        return this.restoreFlag().map(flag -> (T) flag.restore(this.value));
    }

    public Exceptional<RegionFlag<?>> restoreFlag() {
        Exceptional<RegionFlag<?>> flag = Hartshorn.context().get(RegionService.class).flag(this.id());
        if (flag.absent()) {
            if (this.flag == null) return Exceptional.of(new IllegalStateException("Missing flag definition for stored flag '" + this.id + "'"));
            else flag = Exceptional.of(this.flag);
        }
        //noinspection unchecked
        return Exceptional.of(Hartshorn.context().get(flag.get().getClass(), this.id, flag.get().description()));
    }
}
