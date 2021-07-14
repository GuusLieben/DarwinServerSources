package org.dockbox.hartshorn.blockregistry.init;

import org.dockbox.hartshorn.blockregistry.VariantIdentifier;
import org.dockbox.hartshorn.blockregistry.init.VanillaProps.ModGroups;
import org.dockbox.hartshorn.blockregistry.init.VanillaProps.TypeList;

public final class GrassGroundInit {

    private GrassGroundInit() {}

    public static void init(TypeList types) {
        VanillaProps.grass()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("grass_covered_limestone")
            .manual()
            .grassColor()
            .register(TypeList.of(VariantIdentifier.FULL));
        VanillaProps.grassLike()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("grass_covered_limestone")
            .family("grass_covered_limestone")
            .manual()
            .grassColor()
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB,
                VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.grassLike()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("minecraft:grass_block")
            .manual()
            .grassColor()
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.LAYER, VariantIdentifier.STAIRS,
                VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.grass()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("clover_covered_grass")
            .manual()
            .grassColor()
            .register(TypeList.of(VariantIdentifier.FULL));
        VanillaProps.grassLike()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("clover_covered_grass")
            .manual()
            .grassColor()
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.LAYER, VariantIdentifier.STAIRS,
                VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.grass()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("brown_sphagnum_moss_block")
            .texture("block/8_topography/4_grass_leaves/brown_sphagnum_moss")
            .register(types);
        VanillaProps.grass()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("green_sphagnum_moss_block")
            .texture("block/8_topography/4_grass_leaves/green_sphagnum_moss")
            .register(types);
        VanillaProps.grass()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("light_green_sphagnum_moss_block")
            .texture("block/8_topography/4_grass_leaves/light_green_sphagnum_moss")
            .register(types);
        VanillaProps.grass()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("red_sphagnum_moss_block")
            .texture("block/8_topography/4_grass_leaves/red_sphagnum_moss")
            .register(types);
        VanillaProps.grass()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("yellow_sphagnum_moss_block")
            .texture("block/8_topography/4_grass_leaves/yellow_sphagnum_moss")
            .register(types);
        VanillaProps.grass()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("minecraft:mycelium")
            .texture("top", "minecraft:block/mycelium_top")
            .texture("bottom", "minecraft:block/dirt")
            .texture("texture", "minecraft:block/mycelium_top")
            .texture("*", "minecraft:block/mycelium_side")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.LAYER, VariantIdentifier.STAIRS,
                VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER,
                VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.grass()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("vibrant_autumnal_forest_floor")
            .texture("top", "minecraft:block/podzol_top")
            .texture("bottom", "minecraft:block/dirt")
            .texture("texture", "minecraft:block/podzol_top")
            .texture("*", "minecraft:block/podzol_side")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.LAYER, VariantIdentifier.STAIRS,
                VariantIdentifier.VERTICAL_SLAB
                , VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.grass()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("taiga_forest_floor")
            .texture("top", "block/8_topography/4_grass_leaves/taiga_forest_floor")
            .texture("bottom", "minecraft:block/dirt")
            .texture("texture", "block/8_topography/4_grass_leaves/taiga_forest_floor")
            .texture("*", "block/8_topography/4_grass_leaves/taiga_forest_floor_side")
            .register(types);
        VanillaProps.grass()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("fir_forest_floor")
            .texture("bottom", "minecraft:block/dirt")
            .texture("*", "block/8_topography/4_grass_leaves/fir_forest_floor")
            .register(types);
        VanillaProps.grass()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("autumnal_forest_floor_with_roots")
            .texture("bottom", "minecraft:block/dirt")
            .texture("*", "block/8_topography/4_grass_leaves/autumnal_forest_floor_with_roots")
            .register(types);
        VanillaProps.grass()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("mossy_forest_floor_with_roots")
            .texture("bottom", "minecraft:block/dirt")
            .texture("*", "block/8_topography/4_grass_leaves/mossy_forest_floor_with_roots")
            .register(types);
        VanillaProps.grass()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("muddy_autumnal_forest_floor")
            .texture("bottom", "minecraft:block/dirt")
            .texture("*", "block/8_topography/4_grass_leaves/muddy_autumnal_forest_floor")
            .register(types);
        VanillaProps.grass()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("autumnal_forest_floor")
            .texture("bottom", "minecraft:block/dirt")
            .texture("*", "block/8_topography/4_grass_leaves/autumnal_forest_floor")
            .register(types);
        VanillaProps.grass()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("lorien_forest_floor")
            .texture("bottom", "minecraft:block/dirt")
            .texture("*", "block/8_topography/4_grass_leaves/lorien_forest_floor")
            .register(types);
        VanillaProps.grass()
            .group(ModGroups.GRASS_AND_DIRT)
            .name("mossy_lorien_forest_floor")
            .texture("bottom", "minecraft:block/dirt")
            .texture("*", "block/8_topography/4_grass_leaves/mossy_lorien_forest_floor")
            .register(types);
    }
}
