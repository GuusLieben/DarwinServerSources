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

package org.dockbox.hartshorn.blockregistry.init;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import org.dockbox.hartshorn.blockregistry.BlockDataBuilder;
import org.dockbox.hartshorn.blockregistry.VariantIdentifier;
import org.dockbox.hartshorn.persistence.mapping.JacksonObjectMapper;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public final class VanillaProps {

    private VanillaProps() {}

    public static BlockDataBuilder stone() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder planks() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder leaves() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder metal() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder grassyEarth() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder earth() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder glass() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder logs() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder cloth() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder wood() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder woodLike() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder plants() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder mosaic() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder grass() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder grassLike() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder leafLike() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder plantLike() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder grassyStone() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder ice() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder sand() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder grassySand() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder gravel() {
        return new BlockDataBuilder();
    }
    public static BlockDataBuilder grassyGravel() {
        return new BlockDataBuilder();
    }

    public enum ModGroups {
        WATER_AND_AIR,
        TOOL_BLOCKS,
        ROOFING,
        WINDOWS_AND_GLASS,
        MOSAICS_TILES_AND_FLOORS,
        HALF_TIMBERED_WALLS,
        GRASS_AND_DIRT,
        CROPS,
        SAND_AND_GRAVEL,
        PLASTER_STUCCO_AND_PAINT,
        LOGS,
        LEAVES,
        ANIMALS,
        UTILITY,
        PLANKS_AND_BEAMS,
        STORAGE,
        FOOD_BLOCKS,
        METAL,
        APPLIANCES,
        FURNITURE,
        COBBLE_AND_BRICK,
        GRASSES_AND_SHRUBS,
        ADVANCED_CARPENTRY,
        FLOWERS,
        ADVANCED_MASONRY_AND_CERAMICS,
        CLOTH_AND_FIBERS,
        COLUMNS,
        DECORATIONS,
        STONE,
        LIGHTING;
    }

    public enum SoundType {
         WOOL, BAMBOO, WATER
    }

    public enum RenderLayer {
         TRANSLUCENT, CUTOUT, CUTOUT_MIPPED;
    }

    public record TypeList(List<VariantIdentifier> variantIdentifiers)
    {
        public static TypeList of(Class<?> type)
        {
            return of();
        }

        public static TypeList of(VariantIdentifier... variantIdentifiers)
        {
            return new TypeList(HartshornUtils.asList(variantIdentifiers));
        }
    }

    public static final List<String> names = new ArrayList<>();

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, ClassNotFoundException {
        for (ClassInfo info : ClassPath.from(VanillaProps.class.getClassLoader()).getTopLevelClasses("org.dockbox.hartshorn.palswap.init")) {
            if (info.getSimpleName().endsWith("Init")) {
                Class.forName(info.getName()).getMethod("init").invoke(null);
            }
        }
        System.out.println(names.size());
        new JacksonObjectMapper().write(names).get();
    }
}
