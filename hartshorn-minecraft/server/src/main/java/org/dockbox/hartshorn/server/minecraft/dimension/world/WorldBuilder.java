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

package org.dockbox.hartshorn.server.minecraft.dimension.world;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.domain.tuple.Vector3N;
import org.dockbox.hartshorn.di.annotations.inject.Required;
import org.dockbox.hartshorn.server.minecraft.dimension.world.generation.Difficulty;
import org.dockbox.hartshorn.server.minecraft.dimension.world.generation.GeneratorType;
import org.dockbox.hartshorn.server.minecraft.dimension.world.generation.WorldGenerator;
import org.dockbox.hartshorn.server.minecraft.players.Gamemode;

@Required
public interface WorldBuilder {

    WorldBuilder name(String name);
    WorldBuilder generator(WorldGenerator generator);
    WorldBuilder type(GeneratorType type);
    WorldBuilder gamemode(Gamemode mode);
    WorldBuilder difficulty(Difficulty difficulty);
    WorldBuilder loadOnStartup(boolean load);
    WorldBuilder pvp(boolean pvp);
    WorldBuilder spawnPosition(Vector3N position);
    Exceptional<World> build();

}