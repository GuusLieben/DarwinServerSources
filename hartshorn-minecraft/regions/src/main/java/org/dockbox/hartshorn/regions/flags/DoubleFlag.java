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

package org.dockbox.hartshorn.regions.flags;

import org.dockbox.hartshorn.api.exceptions.Except;
import org.dockbox.hartshorn.di.annotations.inject.Bound;
import org.dockbox.hartshorn.i18n.common.ResourceEntry;

public class DoubleFlag extends AbstractRegionFlag<Double> {

    @Bound
    public DoubleFlag(final String id, final ResourceEntry description) {
        super(id, description);
    }

    @Override
    public String serialize(final Double object) {
        return String.valueOf(object);
    }

    @Override
    public Double restore(final String raw) {
        try {
            return Double.parseDouble(raw);
        }
        catch (final NumberFormatException e) {
            Except.handle("Could not restore number flag: " + this.id(), e);
            return -1D;
        }
    }

    @Override
    public Class<Double> type() {
        return Double.class;
    }
}
