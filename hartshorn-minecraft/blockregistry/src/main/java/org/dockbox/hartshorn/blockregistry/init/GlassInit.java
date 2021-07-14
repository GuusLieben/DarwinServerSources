package org.dockbox.hartshorn.blockregistry.init;

import org.dockbox.hartshorn.blockregistry.VariantIdentifier;
import org.dockbox.hartshorn.blockregistry.init.VanillaProps.ModGroups;
import org.dockbox.hartshorn.blockregistry.init.VanillaProps.TypeList;

public final class GlassInit {

    private GlassInit() {}

    public static void init() {
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("tree_decorated_window")
            .texture("block/2_advanced_refined/6_glass/tree_decorated_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("arabian_arched_window")
            .texture("middle", "block/2_advanced_refined/6_glass/arabian_arched_window_middle")
            .texture("*", "block/2_advanced_refined/6_glass/arabian_arched_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("minecraft:white_stained_glass")
            .texture("minecraft:block/white_stained_glass")
            .register(TypeList.of(VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("minecraft:orange_stained_glass")
            .texture("minecraft:block/orange_stained_glass")
            .register(TypeList.of(VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("minecraft:magenta_stained_glass")
            .texture("minecraft:block/magenta_stained_glass")
            .register(TypeList.of(VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("minecraft:light_blue_stained_glass")
            .texture("minecraft:block/light_blue_stained_glass")
            .register(TypeList.of(VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("minecraft:yellow_stained_glass")
            .texture("minecraft:block/yellow_stained_glass")
            .register(TypeList.of(VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("minecraft:lime_stained_glass")
            .texture("minecraft:block/lime_stained_glass")
            .register(TypeList.of(VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("minecraft:pink_stained_glass")
            .texture("minecraft:block/pink_stained_glass")
            .register(TypeList.of(VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("minecraft:gray_stained_glass")
            .texture("minecraft:block/gray_stained_glass")
            .register(TypeList.of(VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("minecraft:light_gray_stained_glass")
            .texture("minecraft:block/light_gray_stained_glass")
            .register(TypeList.of(VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("minecraft:cyan_stained_glass")
            .texture("minecraft:block/cyan_stained_glass")
            .register(TypeList.of(VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("minecraft:purple_stained_glass")
            .texture("minecraft:block/purple_stained_glass")
            .register(TypeList.of(VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("minecraft:blue_stained_glass")
            .texture("minecraft:block/blue_stained_glass")
            .register(TypeList.of(VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("minecraft:brown_stained_glass")
            .texture("minecraft:block/brown_stained_glass")
            .register(TypeList.of(VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("minecraft:green_stained_glass")
            .texture("minecraft:block/green_stained_glass")
            .register(TypeList.of(VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("minecraft:red_stained_glass")
            .texture("minecraft:block/red_stained_glass")
            .register(TypeList.of(VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("minecraft:black_stained_glass")
            .texture("minecraft:block/black_stained_glass")
            .register(TypeList.of(VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("circular_glass_1")
            .texture("minecraft:block/glass")
            .register(TypeList.of(VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("circular_glass_2")
            .texture("minecraft:block/glass")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS,  VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("straight_glass_1")
            .texture("block/2_advanced_refined/6_glass/straight_glass")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("straight_glass_2")
            .texture("block/2_advanced_refined/6_glass/straight_glass")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("fancy_dragon_border_glass")
            .texture("block/2_advanced_refined/6_glass/fancy_dragon_border_glass")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("fancy_sharp_arch_glass")
            .texture("block/2_advanced_refined/6_glass/fancy_sharp_arch_glass")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("simple_brown_wooden_frame_window")
            .texture("block/2_advanced_refined/6_glass/simple_brown_wooden_frame_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("elongated_brown_wooden_frame_window")
            .texture("middle", "block/2_advanced_refined/6_glass/elongated_brown_wooden_frame_window_middle")
            .texture("*", "block/2_advanced_refined/6_glass/elongated_brown_wooden_frame_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("arched_brown_wooden_frame_window")
            .texture("block/2_advanced_refined/6_glass/arched_brown_wooden_frame_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("round_brown_wooden_frame_window")
            .texture("block/2_advanced_refined/6_glass/round_brown_wooden_frame_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("simple_yellow_wooden_frame_window")
            .texture("block/2_advanced_refined/6_glass/simple_yellow_wooden_frame_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("elongated_yellow_wooden_frame_window")
            .texture("middle", "block/2_advanced_refined/6_glass/elongated_yellow_wooden_frame_window_middle")
            .texture("*", "block/2_advanced_refined/6_glass/elongated_yellow_wooden_frame_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("arched_yellow_wooden_frame_window")
            .texture("block/2_advanced_refined/6_glass/arched_yellow_wooden_frame_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("round_yellow_wooden_frame_window")
            .texture("block/2_advanced_refined/6_glass/round_yellow_wooden_frame_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("simple_green_wooden_frame_window")
            .texture("block/2_advanced_refined/6_glass/simple_green_wooden_frame_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("elongated_green_wooden_frame_window")
            .texture("middle", "block/2_advanced_refined/6_glass/elongated_green_wooden_frame_window_middle")
            .texture("*", "block/2_advanced_refined/6_glass/elongated_green_wooden_frame_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("arched_green_wooden_frame_window")
            .texture("block/2_advanced_refined/6_glass/arched_green_wooden_frame_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("round_green_wooden_frame_window")
            .texture("block/2_advanced_refined/6_glass/round_green_wooden_frame_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("simple_white_wooden_frame_window")
            .texture("block/2_advanced_refined/6_glass/simple_white_wooden_frame_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.SLAB, VariantIdentifier.STAIRS, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("elongated_white_wooden_frame_window")
            .texture("middle", "block/2_advanced_refined/6_glass/elongated_white_wooden_frame_window_middle")
            .texture("*", "block/2_advanced_refined/6_glass/elongated_white_wooden_frame_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("arched_white_wooden_frame_window")
            .texture("block/2_advanced_refined/6_glass/arched_white_wooden_frame_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("round_white_wooden_frame_window")
            .texture("block/2_advanced_refined/6_glass/round_white_wooden_frame_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("asian_window")
            .texture("middle", "block/2_advanced_refined/6_glass/asian_window_middle")
            .texture("*", "block/2_advanced_refined/6_glass/asian_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("red_asian_window")
            .texture("middle", "block/2_advanced_refined/6_glass/red_asian_window_middle")
            .texture("*", "block/2_advanced_refined/6_glass/red_asian_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.planks()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("shoji")
            .texture("block/2_advanced_refined/7_wood/shoji")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.planks()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("damaged_shoji")
            .texture("block/2_advanced_refined/7_wood/damaged_shoji")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.planks()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("vertical_shoji")
            .texture("block/2_advanced_refined/7_wood/vertical_shoji")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.planks()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("vertical_damaged_shoji")
            .texture("block/2_advanced_refined/7_wood/vertical_damaged_shoji")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("small_diamond_pattern_glass_window")
            .texture("middle", "block/2_advanced_refined/6_glass/small_diamond_pattern_glass_window_middle")
            .texture("*", "block/2_advanced_refined/6_glass/small_diamond_pattern_glass_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("diagonal_square_pattern_glass_window")
            .texture("middle", "block/2_advanced_refined/6_glass/diagonal_square_pattern_glass_window_middle")
            .texture("*", "block/2_advanced_refined/6_glass/diagonal_square_pattern_glass_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("simple_square_pattern_glass_window")
            .texture("middle", "block/2_advanced_refined/6_glass/simple_square_pattern_glass_window_middle")
            .texture("*", "block/2_advanced_refined/6_glass/simple_square_pattern_glass_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("white_plaster_with_circular_window")
            .texture("block/2_advanced_refined/6_glass/white_plaster_with_circular_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("white_plaster_with_reinforced_circular_window")
            .texture("block/2_advanced_refined/6_glass/white_plaster_with_reinforced_circular_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("yellow_plaster_with_circular_window")
            .texture("block/2_advanced_refined/6_glass/yellow_plaster_with_circular_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("yellow_plaster_with_reinforced_circular_window")
            .texture("block/2_advanced_refined/6_glass/yellow_plaster_with_reinforced_circular_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("limestone_gothic_window")
            .texture("block/2_advanced_refined/6_glass/limestone_gothic_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("light_limestone_gothic_window")
            .texture("block/2_advanced_refined/6_glass/light_limestone_gothic_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("sandstone_gothic_window")
            .texture("block/2_advanced_refined/6_glass/sandstone_gothic_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("tan_sandstone_gothic_window")
            .texture("block/2_advanced_refined/6_glass/tan_sandstone_gothic_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("granite_art_noveau_window")
            .texture("middle", "block/2_advanced_refined/6_glass/granite_art_noveau_window_middle")
            .texture("*", "block/2_advanced_refined/6_glass/granite_art_noveau_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("simple_arch_window")
            .texture("middle", "block/2_advanced_refined/6_glass/simple_arch_window_middle")
            .texture("*", "block/2_advanced_refined/6_glass/simple_arch_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("white_roundel_leaded_glass")
            .texture("*", "block/2_advanced_refined/6_glass/white_roundel_leaded_glass")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("stained_glass_gothic_window")
            .texture("middle","block/2_advanced_refined/6_glass/stained_glass_gothic_window_middle")
            .texture("*","block/2_advanced_refined/6_glass/stained_glass_gothic_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("lattice_window_panel")
            .texture("middle", "block/2_advanced_refined/6_glass/lattice_window_panel_middle")
            .texture("*", "block/2_advanced_refined/6_glass/lattice_window_panel")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.glass()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("lattice_window_panel_1")
            .texture("block/2_advanced_refined/6_glass/lattice_window_panel_1")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.wood()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("wooden_arch_window")
            .texture("top", "block/2_advanced_refined/6_glass/wooden_window_frame_top")
            .texture("bottom", "block/2_advanced_refined/6_glass/wooden_window_frame_top")
            .texture("*", "block/2_advanced_refined/6_glass/wooden_arch_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.wood()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("wooden_gothic_window")
            .texture("top", "block/2_advanced_refined/6_glass/wooden_window_frame_top")
            .texture("bottom", "block/2_advanced_refined/6_glass/wooden_window_frame_top")
            .texture("*", "block/2_advanced_refined/6_glass/wooden_gothic_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
        VanillaProps.wood()
            .group(ModGroups.WINDOWS_AND_GLASS)
            .name("wooden_mullion_window")
            .texture("top", "block/2_advanced_refined/6_glass/wooden_window_frame_top")
            .texture("bottom", "block/2_advanced_refined/6_glass/wooden_window_frame_top")
            .texture("*", "block/2_advanced_refined/6_glass/wooden_mullion_window")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.PANE, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER));
    }
}
