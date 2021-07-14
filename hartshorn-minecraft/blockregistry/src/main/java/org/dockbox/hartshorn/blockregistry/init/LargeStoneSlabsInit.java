package org.dockbox.hartshorn.blockregistry.init;

import org.dockbox.hartshorn.blockregistry.init.VanillaProps.ModGroups;
import org.dockbox.hartshorn.blockregistry.init.VanillaProps.TypeList;

public final class LargeStoneSlabsInit {

    private LargeStoneSlabsInit() {}

    public static void init(TypeList types, TypeList typesVanilla, TypeList typesVanillaNoStairs) {

        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("large_limestone_block")
                .texture("top", "block/1_basic_refined/1_stone/3_limestone/large_limestone_block_topbottom")
                .texture("bottom", "block/1_basic_refined/1_stone/3_limestone/large_limestone_block_topbottom")
                .texture("*", "block/1_basic_refined/1_stone/3_limestone/large_limestone_block")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("large_white_marble_block")
                .texture("top", "block/1_basic_refined/1_stone/4_marble/large_white_marble_block_topbottom")
                .texture("bottom", "block/1_basic_refined/1_stone/4_marble/large_white_marble_block_topbottom")
                .texture("*", "block/1_basic_refined/1_stone/4_marble/large_white_marble_block")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("smooth_large_white_marble_block")
                .texture("top", "block/1_basic_refined/1_stone/4_marble/large_white_marble_block_topbottom")
                .texture("bottom", "block/1_basic_refined/1_stone/4_marble/large_white_marble_block_topbottom")
                .texture("*", "block/1_basic_refined/1_stone/4_marble/smooth_large_white_marble_block")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("large_sandstone_block")
                .texture("top", "block/1_basic_refined/1_stone/5_sandstone/large_sandstone_block_topbottom")
                .texture("bottom", "block/1_basic_refined/1_stone/5_sandstone/large_sandstone_block_topbottom")
                .texture("*", "block/1_basic_refined/1_stone/5_sandstone/large_sandstone_block")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("large_inscribed_sandstone_block")
                .texture("top", "block/1_basic_refined/1_stone/5_sandstone/large_sandstone_block_topbottom")
                .texture("bottom", "block/1_basic_refined/1_stone/5_sandstone/large_sandstone_block_topbottom")
                .texture("*", "block/1_basic_refined/1_stone/5_sandstone/large_inscribed_sandstone_block")
                .register(TypeList.of(Void.class));
        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("smooth_large_sandstone_block")
                .texture("top", "block/1_basic_refined/1_stone/5_sandstone/large_sandstone_block_topbottom")
                .texture("bottom", "block/1_basic_refined/1_stone/5_sandstone/large_sandstone_block_topbottom")
                .texture("*", "block/1_basic_refined/1_stone/5_sandstone/smooth_large_sandstone_block")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("smooth_large_inscribed_sandstone_block")
                .texture("top", "block/1_basic_refined/1_stone/5_sandstone/large_sandstone_block_topbottom")
                .texture("bottom", "block/1_basic_refined/1_stone/5_sandstone/large_sandstone_block_topbottom")
                .texture("*", "block/1_basic_refined/1_stone/5_sandstone/smooth_large_inscribed_sandstone_block")
                .register(TypeList.of(Void.class));
        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("large_tan_sandstone_block")
                .texture("top", "block/1_basic_refined/1_stone/5_sandstone/large_tan_sandstone_block_topbottom")
                .texture("bottom", "block/1_basic_refined/1_stone/5_sandstone/large_tan_sandstone_block_topbottom")
                .texture("*", "block/1_basic_refined/1_stone/5_sandstone/large_tan_sandstone_block")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("large_andesite_brick")
                .texture("block/1_basic_refined/1_stone/large_andesite_brick")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("large_eroded_pentelic_marble_block")
                .texture("block/1_basic_refined/1_stone/4_marble/large_eroded_pentelic_marble_block")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("large_weathered_pentelic_marble_block")
                .texture("block/1_basic_refined/1_stone/4_marble/large_weathered_pentelic_marble_block")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("large_pentelic_marble_block")
                .texture("block/1_basic_refined/1_stone/4_marble/large_pentelic_marble_block")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.PLASTER_STUCCO_AND_PAINT)
                .name("large_black_painted_block")
                .texture("top", "block/1_basic_refined/1_stone/large_black_painted_block_topbottom")
                .texture("bottom", "block/1_basic_refined/1_stone/large_black_painted_block_topbottom")
                .texture("*", "block/1_basic_refined/1_stone/large_black_painted_block")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.PLASTER_STUCCO_AND_PAINT)
                .name("large_inscribed_black_painted_block")
                .texture("top", "block/1_basic_refined/1_stone/large_black_painted_block_topbottom")
                .texture("bottom", "block/1_basic_refined/1_stone/large_black_painted_block_topbottom")
                .texture("*", "block/1_basic_refined/1_stone/large_inscribed_black_painted_block")
                .register(TypeList.of(Void.class));
        VanillaProps.stone()
                .group(ModGroups.PLASTER_STUCCO_AND_PAINT)
                .name("smooth_large_black_painted_block")
                .texture("top", "block/1_basic_refined/1_stone/large_black_painted_block_topbottom")
                .texture("bottom", "block/1_basic_refined/1_stone/large_black_painted_block_topbottom")
                .texture("*", "block/1_basic_refined/1_stone/smooth_large_black_painted_block")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.PLASTER_STUCCO_AND_PAINT)
                .name("smooth_large_inscribed_black_painted_block")
                .texture("top", "block/1_basic_refined/1_stone/large_black_painted_block_topbottom")
                .texture("bottom", "block/1_basic_refined/1_stone/large_black_painted_block_topbottom")
                .texture("*", "block/1_basic_refined/1_stone/smooth_large_inscribed_black_painted_block")
                .register(TypeList.of(Void.class));
        VanillaProps.stone()
                .group(ModGroups.PLASTER_STUCCO_AND_PAINT)
                .name("concrete_wall")
                .texture("minecraft:block/light_gray_concrete")
                .register(typesVanilla);
        VanillaProps.stone()
                .group(ModGroups.PLASTER_STUCCO_AND_PAINT)
                .name("damaged_concrete_wall")
                .texture("block/2_advanced_refined/8_concrete/damaged_concrete_wall")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.PLASTER_STUCCO_AND_PAINT)
                .name("white_concrete")
                .texture("minecraft:block/white_concrete")
                .register(typesVanilla);
        VanillaProps.stone()
                .group(ModGroups.PLASTER_STUCCO_AND_PAINT)
                .name("orange_concrete")
                .texture("minecraft:block/orange_concrete")
                .register(typesVanilla);
        VanillaProps.stone()
                .group(ModGroups.PLASTER_STUCCO_AND_PAINT)
                .name("light_blue_concrete")
                .texture("minecraft:block/light_blue_concrete")
                .register(typesVanilla);
        VanillaProps.stone()
                .group(ModGroups.PLASTER_STUCCO_AND_PAINT)
                .name("yellow_concrete")
                .texture("minecraft:block/yellow_concrete")
                .register(typesVanilla);
        VanillaProps.stone()
                .group(ModGroups.PLASTER_STUCCO_AND_PAINT)
                .name("green_concrete")
                .texture("minecraft:block/green_concrete")
                .register(typesVanilla);
        VanillaProps.stone()
                .group(ModGroups.PLASTER_STUCCO_AND_PAINT)
                .name("pink_concrete")
                .texture("minecraft:block/pink_concrete")
                .register(typesVanilla);
        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("gray_concrete")
                .texture("minecraft:block/gray_concrete")
                .register(typesVanilla);
        VanillaProps.stone()
                .group(ModGroups.METAL)
                .name("carbonite_paneling")
                .texture("minecraft:block/purpur_block")
                .register(typesVanilla);
        VanillaProps.stone()
                .group(ModGroups.PLASTER_STUCCO_AND_PAINT)
                .name("purple_concrete")
                .texture("minecraft:block/purple_concrete")
                .register(typesVanilla);
        VanillaProps.stone()
                .group(ModGroups.PLASTER_STUCCO_AND_PAINT)
                .name("brown_concrete")
                .texture("minecraft:block/brown_concrete")
                .register(typesVanilla);
        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("large_andesite_masonry")
                .texture("minecraft:block/polished_andesite")
                .register(typesVanillaNoStairs);
        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("large_granite_brick")
                .texture("minecraft:block/polished_granite")
                .register(typesVanillaNoStairs);
        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("large_light_limestone_brick")
                .texture("minecraft:block/polished_diorite")
                .register(typesVanillaNoStairs);
    }
}
