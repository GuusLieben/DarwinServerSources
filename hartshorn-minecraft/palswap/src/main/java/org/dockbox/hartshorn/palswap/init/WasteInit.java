package org.dockbox.hartshorn.palswap.init;

import org.dockbox.hartshorn.palswap.init.VanillaProps.ModGroups;
import org.dockbox.hartshorn.palswap.init.VanillaProps.RenderLayer;
import org.dockbox.hartshorn.palswap.init.VanillaProps.TypeList;

public class WasteInit {

    public static void init() {
        VanillaProps.stone()
                .group(ModGroups.ANIMALS)
                .name("hanging_corpse")
                .manual()
                .blocking(false)
                .render(RenderLayer.CUTOUT)
                                .register(TypeList.of(Void.class));
        VanillaProps.stone()
                .group(ModGroups.ANIMALS)
                .name("bones_on_the_ground")
                .manual()
                .blocking(false)
                .render(RenderLayer.CUTOUT)
                                .register(TypeList.of(Void.class));
        VanillaProps.stone()
                .group(ModGroups.ANIMALS)
                .name("cow_patty")
                .manual()
                .blocking(false)
                .render(RenderLayer.CUTOUT)
                                .register(TypeList.of(Void.class));
    }
}
