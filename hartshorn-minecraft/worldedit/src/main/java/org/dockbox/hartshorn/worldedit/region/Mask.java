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

package org.dockbox.hartshorn.worldedit.region;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.di.context.ApplicationContext;
import org.dockbox.hartshorn.server.minecraft.players.Player;
import org.dockbox.hartshorn.worldedit.WorldEditService;

public interface Mask {

    static Exceptional<Mask> parse(final ApplicationContext context, final String mask) {
        return context.get(WorldEditService.class).parseMask(mask, null);
    }

    static Exceptional<Mask> parse(final ApplicationContext context, final String mask, final Player cause) {
        return context.get(WorldEditService.class).parseMask(mask, cause);
    }
}
