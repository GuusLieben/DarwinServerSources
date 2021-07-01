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

package org.dockbox.hartshorn.palswap.init;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import org.dockbox.hartshorn.persistence.mapping.JacksonObjectMapper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class VanillaProps {

    public static Builder stone() {
        return new Builder();
    }
    public static Builder planks() {
        return new Builder();
    }
    public static Builder leaves() {
        return new Builder();
    }
    public static Builder metal() {
        return new Builder();
    }
    public static Builder grassyEarth() {
        return new Builder();
    }
    public static Builder earth() {
        return new Builder();
    }
    public static Builder glass() {
        return new Builder();
    }
    public static Builder logs() {
        return new Builder();
    }
    public static Builder cloth() {
        return new Builder();
    }
    public static Builder wood() {
        return new Builder();
    }
    public static Builder woodLike() {
        return new Builder();
    }
    public static Builder plants() {
        return new Builder();
    }
    public static Builder mosaic() {
        return new Builder();
    }
    public static Builder grass() {
        return new Builder();
    }
    public static Builder grassLike() {
        return new Builder();
    }
    public static Builder leafLike() {
        return new Builder();
    }
    public static Builder plantLike() {
        return new Builder();
    }
    public static Builder grassyStone() {
        return new Builder();
    }
    public static Builder ice() {
        return new Builder();
    }
    public static Builder sand() {
        return new Builder();
    }
    public static Builder grassySand() {
        return new Builder();
    }
    public static Builder gravel() {
        return new Builder();
    }
    public static Builder grassyGravel() {
        return new Builder();
    }

    public static class ModGroups {
        public static String WATER_AND_AIR = "";
        public static String TOOL_BLOCKS = "";
        public static String ROOFING = "";
        public static String WINDOWS_AND_GLASS = "";
        public static String MOSAICS_TILES_AND_FLOORS = "";
        public static String HALF_TIMBERED_WALLS = "";
        public static String GRASS_AND_DIRT = "";
        public static String CROPS = "";
        public static String SAND_AND_GRAVEL = "";
        public static String PLASTER_STUCCO_AND_PAINT = "";
        public static String LOGS = "";
        public static String LEAVES = "";
        public static String ANIMALS = "";
        public static String UTILITY = "";
        public static String PLANKS_AND_BEAMS = "";
        public static String STORAGE = "";
        public static String FOOD_BLOCKS = "";
        public static String METAL = "";
        public static String APPLIANCES = "";
        public static String FURNITURE = "";
        public static String COBBLE_AND_BRICK = "";
        public static String GRASSES_AND_SHRUBS = "";
        public static String ADVANCED_CARPENTRY = "";
        public static String FLOWERS = "";
        public static String ADVANCED_MASONRY_AND_CERAMICS = "";
        public static String CLOTH_AND_FIBERS = "";
        public static String COLUMNS = "";
        public static String DECORATIONS = "";
        public static String STONE = "";
        public static String LIGHTING = "";
    }

    public static class SoundType {
        public static String WOOL = "";
        public static String BAMBOO = "";
    }

    public static class RenderLayer {
        public static String TRANSLUCENT = "";
        public static String CUTOUT = "";
        public static String CUTOUT_MIPPED = "";
    }

    public static class TypeList {
        public static TypeList of(Class<?> type) {
            return new TypeList();
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

    public static class Builder {

        public Builder group(String group) {
            return this;
        }

        public Builder family(String family) {
            return this;
        }

        public Builder name(String name) {
            names.add(name);
            return this;
        }

        public Builder name(String name, String name2) {
            names.add(name);
            names.add(name2);
            return this;
        }

        public Builder sound(String sound) {
            return this;
        }

        public Builder texture(String texture) {
            return this;
        }

        public Builder texture(String name, String texture) {
            return this;
        }

        public Builder blocking(boolean blocking) {
            return this;
        }

        public Builder solid(boolean solid) {
            return this;
        }

        public Builder manual() {
            return this;
        }
        public Builder waterColor() {
            return this;
        }
        public Builder variantFamily() {
            return this;
        }
        public Builder grassColor() {
            return this;
        }
        public Builder foliageColor() {
            return this;
        }

        public Builder with(String name, String value) {
            return this;
        }

        public Builder render(String name) {
            return this;
        }

        public void register(TypeList type) {

        }
        public Builder randomTick(boolean tick) {
            return this;
        }
        public Builder strength(double a, double b) {
            return this;
        }
    }

}
