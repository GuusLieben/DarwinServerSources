package org.dockbox.hartshorn.blockregistry.init;

import org.dockbox.hartshorn.blockregistry.VariantIdentifier;
import org.dockbox.hartshorn.blockregistry.init.VanillaProps.ModGroups;
import org.dockbox.hartshorn.blockregistry.init.VanillaProps.TypeList;

public final class ThatchInit {

    private ThatchInit() {}

    public static void init(TypeList types) {
        VanillaProps.plantLike()
            .group(ModGroups.CROPS)
            .name("bundled_hay")
            .texture("top", "minecraft:block/hay_block_top")
            .texture("bottom", "minecraft:block/hay_block_top")
            .texture("*", "minecraft:block/hay_block_side")
            .register(TypeList.of(VariantIdentifier.SLAB, VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS));
        VanillaProps.plantLike()
            .group(ModGroups.CROPS)
            .name("hay_bale")
            .texture("end", "block/1_basic_refined/2_roof/2_thatch/hay_bale_topbottom")
            .texture("top", "block/1_basic_refined/2_roof/2_thatch/hay_bale_topbottom")
            .texture("bottom", "block/1_basic_refined/2_roof/2_thatch/hay_bale_topbottom")
            .texture("*", "block/1_basic_refined/2_roof/2_thatch/hay_bale")
            .register(TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SLAB, VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS));
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("dark_brown_thatch")
            .texture("block/1_basic_refined/2_roof/2_thatch/dark_brown_thatch")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("dark_brown_thatch_tracery")
            .texture("bottom","block/1_basic_refined/2_roof/2_thatch/dark_brown_thatch")
            .texture("top","block/1_basic_refined/2_roof/2_thatch/dark_brown_thatch")
            .texture("*","block/1_basic_refined/2_roof/2_thatch/dark_brown_thatch_tracery")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("mossy_dark_brown_thatch")
            .texture("block/1_basic_refined/2_roof/2_thatch/mossy_dark_brown_thatch")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("overgrown_mossy_dark_brown_thatch")
            .texture("block/1_basic_refined/2_roof/2_thatch/overgrown_mossy_dark_brown_thatch")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("dark_gray_thatch")
            .texture("block/1_basic_refined/2_roof/2_thatch/dark_gray_thatch")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("dark_gray_thatch_tracery")
            .texture("bottom","block/1_basic_refined/2_roof/2_thatch/dark_gray_thatch")
            .texture("top","block/1_basic_refined/2_roof/2_thatch/dark_gray_thatch")
            .texture("*","block/1_basic_refined/2_roof/2_thatch/dark_gray_thatch_tracery")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("overgrown_mossy_dark_gray_thatch")
            .texture("block/1_basic_refined/2_roof/2_thatch/overgrown_mossy_dark_gray_thatch")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("dark_yellow_thatch")
            .texture("block/1_basic_refined/2_roof/2_thatch/dark_yellow_thatch")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("dark_yellow_thatch_tracery")
            .texture("bottom","block/1_basic_refined/2_roof/2_thatch/dark_yellow_thatch")
            .texture("top","block/1_basic_refined/2_roof/2_thatch/dark_yellow_thatch")
            .texture("*","block/1_basic_refined/2_roof/2_thatch/dark_yellow_thatch_tracery")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("mossy_dark_yellow_thatch")
            .texture("block/1_basic_refined/2_roof/2_thatch/mossy_dark_yellow_thatch")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("yellow_thatch")
            .texture("block/1_basic_refined/2_roof/2_thatch/yellow_thatch")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("mossy_yellow_thatch")
            .texture("block/1_basic_refined/2_roof/2_thatch/mossy_yellow_thatch")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("yellow_thatch_tracery")
            .texture("bottom","block/1_basic_refined/2_roof/2_thatch/yellow_thatch")
            .texture("top","block/1_basic_refined/2_roof/2_thatch/yellow_thatch")
            .texture("*","block/1_basic_refined/2_roof/2_thatch/yellow_thatch_tracery")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("brown_thatch")
            .texture("block/1_basic_refined/2_roof/2_thatch/brown_thatch")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("mossy_brown_thatch")
            .texture("block/1_basic_refined/2_roof/2_thatch/mossy_brown_thatch")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("brown_thatch_tracery")
            .texture("bottom","block/1_basic_refined/2_roof/2_thatch/brown_thatch")
            .texture("top","block/1_basic_refined/2_roof/2_thatch/brown_thatch")
            .texture("*","block/1_basic_refined/2_roof/2_thatch/brown_thatch_tracery")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("gray_thatch")
            .texture("block/1_basic_refined/2_roof/2_thatch/gray_thatch")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("mossy_gray_thatch")
            .texture("block/1_basic_refined/2_roof/2_thatch/mossy_gray_thatch")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("overgrown_mossy_gray_thatch")
            .texture("block/1_basic_refined/2_roof/2_thatch/overgrown_mossy_gray_thatch")
            .register(types);
        VanillaProps.plantLike()
            .group(ModGroups.ROOFING)
            .name("gray_thatch_tracery")
            .texture("bottom","block/1_basic_refined/2_roof/2_thatch/gray_thatch")
            .texture("top","block/1_basic_refined/2_roof/2_thatch/gray_thatch")
            .texture("*","block/1_basic_refined/2_roof/2_thatch/gray_thatch_tracery")
            .register(types);
    }
}
