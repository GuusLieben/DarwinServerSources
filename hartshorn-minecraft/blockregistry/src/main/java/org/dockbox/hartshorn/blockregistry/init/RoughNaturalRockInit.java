package org.dockbox.hartshorn.blockregistry.init;

import org.dockbox.hartshorn.blockregistry.VariantIdentifier;
import org.dockbox.hartshorn.blockregistry.init.VanillaProps.ModGroups;
import org.dockbox.hartshorn.blockregistry.init.VanillaProps.TypeList;

public final class RoughNaturalRockInit {

    private RoughNaturalRockInit() {}

    public static void init(TypeList types, TypeList typesRocks, TypeList typesVanilla, TypeList typesRocksVanilla) {
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("minecraft:prismarine")
            .texture("minecraft:block/prismarine")
            .register(typesVanilla);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("red_granite")
            .texture("block/8_topography/1_stone/red_granite")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("gray_quartzite")
            .texture("block/8_topography/1_stone/gray_quartzite")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("pink_quartzite")
            .texture("block/8_topography/1_stone/pink_quartzite")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("yellow_quartzite")
            .texture("block/8_topography/1_stone/yellow_quartzite")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("icy_limestone")
            .texture("block/8_topography/1_stone/icy_limestone")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("mossy_limestone")
            .texture("block/8_topography/1_stone/mossy_limestone")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("blue_schist")
            .texture("top", "block/8_topography/1_stone/blue_schist_topbottom")
            .texture("bottom", "block/8_topography/1_stone/blue_schist_topbottom")
            .texture("*", "block/8_topography/1_stone/blue_schist")
            .register(typesRocks);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("lichen_blue_schist")
            .texture("top", "block/8_topography/1_stone/blue_schist_topbottom")
            .texture("bottom", "block/8_topography/1_stone/blue_schist_topbottom")
            .texture("*", "block/8_topography/1_stone/lichen_blue_schist")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("mossy_blue_schist")
            .texture("top", "block/8_topography/1_stone/blue_schist_topbottom")
            .texture("bottom", "block/8_topography/1_stone/blue_schist_topbottom")
            .texture("*", "block/8_topography/1_stone/mossy_blue_schist")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("weathered_blue_schist")
            .texture("top", "block/8_topography/1_stone/blue_schist_topbottom")
            .texture("bottom", "block/8_topography/1_stone/blue_schist_topbottom")
            .texture("*", "block/8_topography/1_stone/weathered_blue_schist")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("green_schist")
            .texture("top", "block/8_topography/1_stone/green_schist_topbottom")
            .texture("bottom", "block/8_topography/1_stone/green_schist_topbottom")
            .texture("*", "block/8_topography/1_stone/green_schist")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("serpentinite")
            .texture("block/8_topography/1_stone/serpentinite")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("gneiss")
            .texture("block/8_topography/1_stone/gneiss")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("eroded_gneiss")
            .texture("block/8_topography/1_stone/eroded_gneiss")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("shale")
            .texture("top", "block/8_topography/1_stone/shale_topbottom")
            .texture("bottom", "block/8_topography/1_stone/shale_topbottom")
            .texture("*", "block/8_topography/1_stone/shale")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("graywacke")
            .texture("top", "block/8_topography/1_stone/graywacke_topbottom")
            .texture("bottom", "block/8_topography/1_stone/graywacke_topbottom")
            .texture("*", "block/8_topography/1_stone/graywacke")
            .register(typesRocks);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("chalk")
            .texture("block/8_topography/1_stone/chalk")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("layered_chalk")
            .texture("block/8_topography/1_stone/layered_chalk")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("weathered_andesite")
            .texture("block/8_topography/1_stone/weathered_andesite")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("weathered_granite")
            .texture("block/8_topography/1_stone/weathered_granite")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("snowy_granite")
            .texture("block/8_topography/1_stone/snowy_granite")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("orange_sandstone")
            .texture("top", "block/8_topography/1_stone/orange_sandstone_topbottom")
            .texture("bottom", "block/8_topography/1_stone/orange_sandstone_topbottom")
            .texture("*", "block/8_topography/1_stone/orange_sandstone")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("mudstone")
            .texture("top", "block/8_topography/1_stone/mudstone_topbottom")
            .texture("bottom", "block/8_topography/1_stone/mudstone_topbottom")
            .texture("*", "block/8_topography/1_stone/mudstone")
            .register(typesRocks);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("coastal_red_sandstone")
            .texture("top", "block/8_topography/1_stone/coastal_red_sandstone_topbottom")
            .texture("bottom", "block/8_topography/1_stone/coastal_red_sandstone_topbottom")
            .texture("*", "block/8_topography/1_stone/coastal_red_sandstone")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("red_sandstone")
            .texture("top", "block/8_topography/1_stone/red_sandstone_topbottom")
            .texture("bottom", "block/8_topography/1_stone/red_sandstone_topbottom")
            .texture("*", "block/8_topography/1_stone/red_sandstone")
            .register(typesRocks);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("mossy_red_sandstone")
            .texture("top", "block/8_topography/1_stone/mossy_red_sandstone_topbottom")
            .texture("bottom", "block/8_topography/1_stone/mossy_red_sandstone_topbottom")
            .texture("*", "block/8_topography/1_stone/mossy_red_sandstone")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("tan_sandstone")
            .texture("top", "block/8_topography/1_stone/tan_sandstone_topbottom")
            .texture("bottom", "block/8_topography/1_stone/tan_sandstone_topbottom")
            .texture("*", "block/8_topography/1_stone/tan_sandstone")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("brown_sandstone")
            .texture("top", "block/8_topography/1_stone/brown_sandstone_topbottom")
            .texture("bottom", "block/8_topography/1_stone/brown_sandstone_topbottom")
            .texture("*", "block/8_topography/1_stone/brown_sandstone")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("light_brown_mudstone")
            .texture("block/8_topography/1_stone/light_brown_mudstone")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("umbre_mudstone")
            .texture("minecraft:block/terracotta")
            .register(typesVanilla);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("light_mudstone")
            .texture("minecraft:block/white_terracotta")
            .register(typesVanilla);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("orange_mudstone")
            .texture("minecraft:block/orange_terracotta")
            .register(typesVanilla);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("yellow_mudstone")
            .texture("minecraft:block/yellow_terracotta")
            .register(typesVanilla);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("gray_cave_silt")
            .texture("minecraft:block/gray_terracotta")
            .register(typesVanilla);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("brown_mudstone")
            .texture("minecraft:block/brown_terracotta")
            .register(typesVanilla);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("red_mudstone")
            .texture("minecraft:block/red_terracotta")
            .register(typesVanilla);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("red_clay")
            .texture("block/8_topography/1_stone/red_clay")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("black_hardened_clay")
            .texture("minecraft:block/black_terracotta")
            .register(typesVanilla);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("pahoehoe")
            .texture("block/8_topography/1_stone/pahoehoe")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("magma")
            .texture("minecraft:block/magma")
            .register(typesVanilla);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("obsidian")
            .texture("minecraft:block/obsidian")
            .register(typesRocksVanilla);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("rough_calcite")
            .texture("top", "block/8_topography/1_stone/rough_calcite_topbottom")
            .texture("bottom", "block/8_topography/1_stone/rough_calcite_topbottom")
            .texture("*", "block/8_topography/1_stone/rough_calcite")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("end_stone")
            .texture("minecraft:block/end_stone")
            .register(typesVanilla);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("peridotite")
            .texture("minecraft:block/bedrock")
            .register(typesVanilla);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("snow_covered_icy_limestone")
            .manual()
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SLAB, VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS));
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("moss_covered_limestone")
            .texture("top", "block/8_topography/1_stone/moss_covered_limestone_top")
            .texture("bottom", "block/8_topography/1_stone/mossy_limestone")
            .texture("*", "block/8_topography/1_stone/moss_covered_limestone")
            .register(types);
        VanillaProps.ice()
            .group(ModGroups.WATER_AND_AIR)
            .name("dirty_glacier_ice")
            .texture("block/8_topography/7_ice_snow/dirty_glacier_ice")
            .register(types);
        VanillaProps.ice()
            .group(ModGroups.WATER_AND_AIR)
            .name("glacier_ice")
            .texture("block/8_topography/7_ice_snow/glacier_ice")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.ANIMALS)
            .name("wall_of_skulls_and_bones")
            .texture("top", "block/9_organic/6_waste/bone_wall_with_skeleton_top")
            .texture("bottom", "block/9_organic/6_waste/bone_wall_with_skeleton_top")
            .texture("*", "block/9_organic/6_waste/wall_of_skulls_and_bones")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.ANIMALS)
            .name("bone_wall_with_skeleton")
            .texture("top", "block/9_organic/6_waste/bone_wall_with_skeleton_top")
            .texture("bottom", "block/9_organic/6_waste/bone_wall_with_skeleton_top")
            .texture("*", "block/9_organic/6_waste/bone_wall_with_skeleton")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("stack_of_coal")
            .texture("minecraft:block/coal_block")
            .register(TypeList.of(VariantIdentifier.LAYER));
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("stack_of_glowing_embers")
            .texture("block/8_topography/2_ores_crystals/stack_of_glowing_embers")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.LAYER));
        VanillaProps.stone()
            .group(ModGroups.WATER_AND_AIR)
            .name("minecraft:snow")
            .texture("minecraft:block/snow")
            .register(typesVanilla);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("tuff")
            .texture("*", "block/8_topography/1_stone/tuff")
            .register(typesRocks);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("dark_tuff")
            .texture("*", "block/8_topography/1_stone/dark_tuff")
            .register(typesRocks);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("light_tuff")
            .texture("*", "block/8_topography/1_stone/light_tuff")
            .register(typesRocks);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("dark_obsidian")
            .texture("*", "block/8_topography/1_stone/dark_obsidian")
            .register(typesRocks);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("rough_anorthosite")
            .texture("*", "block/8_topography/1_stone/rough_anorthosite")
            .register(types);
        VanillaProps.stone()
            .group(ModGroups.STONE)
            .name("weathered_anorthosite")
            .texture("*", "block/8_topography/1_stone/weathered_anorthosite")
            .register(types);
    }
}
