package org.dockbox.hartshorn.palswap.init;

import org.dockbox.hartshorn.palswap.init.VanillaProps.ModGroups;
import org.dockbox.hartshorn.palswap.init.VanillaProps.TypeList;

public class WoolInit {

    public static void init() {
        TypeList types = null;
        VanillaProps.cloth()
                .group(ModGroups.CLOTH_AND_FIBERS)
                .name("white_wool")
                .texture("minecraft:block/white_wool")
                                .register(types);
        VanillaProps.cloth()
                .group(ModGroups.CLOTH_AND_FIBERS)
                .name("orange_wool")
                .texture("minecraft:block/orange_wool")
                                .register(types);
        VanillaProps.cloth()
                .group(ModGroups.CLOTH_AND_FIBERS)
                .name("magenta_wool")
                .texture("minecraft:block/magenta_wool")
                                .register(types);
        VanillaProps.cloth()
                .group(ModGroups.CLOTH_AND_FIBERS)
                .name("light_blue_wool")
                .texture("minecraft:block/light_blue_wool")
                                .register(types);
        VanillaProps.cloth()
                .group(ModGroups.CLOTH_AND_FIBERS)
                .name("yellow_wool")
                .texture("minecraft:block/yellow_wool")
                                .register(types);
        VanillaProps.cloth()
                .group(ModGroups.CLOTH_AND_FIBERS)
                .name("lime_wool")
                .texture("minecraft:block/lime_wool")
                                .register(types);
        VanillaProps.cloth()
                .group(ModGroups.CLOTH_AND_FIBERS)
                .name("pink_wool")
                .texture("minecraft:block/pink_wool")
                                .register(types);
        VanillaProps.cloth()
                .group(ModGroups.CLOTH_AND_FIBERS)
                .name("gray_wool")
                .texture("minecraft:block/gray_wool")
                                .register(types);
        VanillaProps.cloth()
                .group(ModGroups.CLOTH_AND_FIBERS)
                .name("light_gray_wool")
                .texture("minecraft:block/light_gray_wool")
                                .register(types);
        VanillaProps.cloth()
                .group(ModGroups.CLOTH_AND_FIBERS)
                .name("cyan_wool")
                .texture("minecraft:block/cyan_wool")
                                .register(types);
        VanillaProps.cloth()
                .group(ModGroups.CLOTH_AND_FIBERS)
                .name("purple_wool")
                .texture("minecraft:block/purple_wool")
                                .register(types);
        VanillaProps.cloth()
                .group(ModGroups.CLOTH_AND_FIBERS)
                .name("blue_wool")
                .texture("minecraft:block/blue_wool")
                                .register(types);
        VanillaProps.cloth()
                .group(ModGroups.CLOTH_AND_FIBERS)
                .name("brown_wool")
                .texture("minecraft:block/brown_wool")
                                .register(types);
        VanillaProps.cloth()
                .group(ModGroups.CLOTH_AND_FIBERS)
                .name("green_wool")
                .texture("minecraft:block/green_wool")
                                .register(types);
        VanillaProps.cloth()
                .group(ModGroups.CLOTH_AND_FIBERS)
                .name("red_wool")
                .texture("minecraft:block/red_wool")
                                .register(types);
        VanillaProps.cloth()
                .group(ModGroups.CLOTH_AND_FIBERS)
                .name("black_wool")
                .texture("minecraft:block/black_wool")
                                .register(types);
    }
}
