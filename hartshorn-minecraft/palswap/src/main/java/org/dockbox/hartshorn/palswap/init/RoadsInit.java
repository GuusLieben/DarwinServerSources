package org.dockbox.hartshorn.palswap.init;

import org.dockbox.hartshorn.palswap.init.VanillaProps.ModGroups;
import org.dockbox.hartshorn.palswap.init.VanillaProps.TypeList;

public class RoadsInit {

    public static void init() {
        TypeList types = null;
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
