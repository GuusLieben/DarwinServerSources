/*
 *  Copyright (C) 2020 Guus Lieben
 *
 *  This framework is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 2.1 of the
 *  License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 *  the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.selene.core.util.world

import java.util.*
import org.dockbox.selene.core.objects.location.Location
import org.dockbox.selene.core.objects.location.World
import org.dockbox.selene.core.objects.tuple.Vector3D

abstract class WorldStorageService {

    abstract fun getLoadedWorlds(): List<World>
    abstract fun getAllWorldUUIDs(): List<UUID>

    abstract fun getWorld(name: String): Optional<World>
    abstract fun getWorld(uuid: UUID): Optional<World>
}
