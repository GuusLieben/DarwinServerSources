package org.dockbox.hartshorn.palswap.init;

import org.dockbox.hartshorn.palswap.init.VanillaProps.ModGroups;
import org.dockbox.hartshorn.palswap.init.VanillaProps.TypeList;

public class SmoothNaturalRockInit {

    public static void init() {
        TypeList types = null;
        TypeList typesRocks = null;
        TypeList typesVanillaNoWall = null;
        TypeList typesVanilla = null;
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("limestone")
                .texture("minecraft:block/stone")
                                .register(typesVanilla);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("pink_limestone")
                .texture("block/8_topography/1_stone/pink_limestone")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("smooth_red_granite")
                .texture("block/8_topography/1_stone/smooth_red_granite")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("light_limestone")
                .texture("minecraft:block/diorite")
                                .register(typesVanillaNoWall);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("dark_limestone")
                .texture("block/8_topography/1_stone/dark_limestone")
                .register(typesRocks);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("andesite")
                .texture("minecraft:block/andesite")
                                .register(typesVanillaNoWall);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("porous_andesite")
                .texture("block/8_topography/1_stone/porous_andesite")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("warped_slate")
                .texture("block/8_topography/1_stone/warped_slate")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("coastal_warped_slate")
                .texture("block/8_topography/1_stone/coastal_warped_slate")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("wet_slate")
                .texture("block/8_topography/1_stone/wet_slate")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("slate")
                .texture("block/8_topography/1_stone/slate")
                .register(typesRocks);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("columnal_basalt")
                .texture("top", "block/8_topography/1_stone/columnal_basalt_top")
                .texture("bottom", "block/8_topography/1_stone/columnal_basalt_top")
                .texture("*", "block/8_topography/1_stone/columnal_basalt")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("dark_rough_marble")
                .texture("block/8_topography/1_stone/dark_rough_marble")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("rough_marble")
                .texture("block/8_topography/1_stone/rough_marble")
                .register(typesRocks);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("light_limestone_boulders")
                .texture("block/8_topography/1_stone/light_limestone_boulders")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("mossy_light_limestone_boulders")
                .texture("block/8_topography/1_stone/mossy_light_limestone_boulders")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("warped_sandstone")
                .texture("block/8_topography/1_stone/warped_sandstone")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("granite")
                .texture("minecraft:block/granite")
                                .register(typesVanillaNoWall);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("dripstone")
                .texture("top", "block/8_topography/1_stone/dripstone_topbottom")
                .texture("bottom", "block/8_topography/1_stone/dripstone_topbottom")
                .texture("*", "block/8_topography/1_stone/dripstone")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("calcite")
                .texture("block/8_topography/1_stone/calcite")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("smooth_obsidian")
                .texture("block/8_topography/1_stone/smooth_obsidian")
                .register(typesRocks);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("smooth_tuff")
                .texture("block/8_topography/1_stone/smooth_tuff")
                .register(typesRocks);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("smooth_serpentinite")
                .texture("block/8_topography/1_stone/smooth_serpentinite")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.STONE)
                .name("smooth_anorthosite")
                .texture("block/8_topography/1_stone/smooth_anorthosite")
                .register(types);
    }
}
