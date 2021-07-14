package org.dockbox.hartshorn.blockregistry.init;

import org.dockbox.hartshorn.blockregistry.init.VanillaProps.ModGroups;
import org.dockbox.hartshorn.blockregistry.init.VanillaProps.TypeList;

public final class RoadsInit {

    private RoadsInit() {}

    public static void init(TypeList types) {

        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("limestone_fanpattern_setts")
                .texture("block/1_basic_refined/1_stone/3_limestone/limestone_fanpattern_setts")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("mixed_fanpattern_setts")
                .texture("block/1_basic_refined/1_stone/mixed_fanpattern_setts")
                .register(types);
        VanillaProps.stone()
                .group(ModGroups.COBBLE_AND_BRICK)
                .name("clay_brick_fanpattern_setts")
                .texture("block/1_basic_refined/1_stone/1_clay/clay_brick_fanpattern_setts")
                .register(types);
    }
}
