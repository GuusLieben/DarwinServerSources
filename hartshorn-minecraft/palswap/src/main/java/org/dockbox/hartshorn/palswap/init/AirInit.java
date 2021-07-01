package org.dockbox.hartshorn.palswap.init;

import org.dockbox.hartshorn.palswap.init.VanillaProps.ModGroups;
import org.dockbox.hartshorn.palswap.init.VanillaProps.RenderLayer;
import org.dockbox.hartshorn.palswap.init.VanillaProps.SoundType;
import org.dockbox.hartshorn.palswap.init.VanillaProps.TypeList;

public class AirInit {

    public static void init() {
        VanillaProps.stone()
                .group(ModGroups.WATER_AND_AIR)
                .name("cloud")
                .sound(SoundType.WOOL)
                .texture("block/8_topography/7_ice_snow/cloud/cloud")
                .blocking(false)
                .solid(false)
                .render(RenderLayer.TRANSLUCENT)
                .register(TypeList.of(Void.class));
        VanillaProps.stone()
                .group(ModGroups.WATER_AND_AIR)
                .name("thick_cloud")
                .sound(SoundType.WOOL)
                .texture("block/8_topography/7_ice_snow/cloud/thick_cloud")
                .blocking(false)
                .solid(false)
                .render(RenderLayer.TRANSLUCENT)
                .register(TypeList.of(Void.class));
        VanillaProps.stone()
                .group(ModGroups.WATER_AND_AIR)
                .name("thin_cloud")
                .sound(SoundType.WOOL)
                .texture("block/8_topography/7_ice_snow/cloud/thin_cloud")
                .blocking(false)
                .solid(false)
                .render(RenderLayer.TRANSLUCENT)
                .register(TypeList.of(Void.class));
        VanillaProps.stone()
                .group(ModGroups.WATER_AND_AIR)
                .name("white_cloud")
                .sound(SoundType.WOOL)
                .blocking(false)
                .solid(false)
                .manual()
                .register(TypeList.of(Void.class));
        VanillaProps.stone()
                .group(ModGroups.WATER_AND_AIR)
                .name("steam")
                .sound(SoundType.WOOL)
                .manual()
                .blocking(false)
                .render(RenderLayer.CUTOUT)
                .register(TypeList.of(Void.class));
        VanillaProps.stone()
                .group(ModGroups.WATER_AND_AIR)
                .name("smoke")
                .sound(SoundType.WOOL)
                .manual()
                .blocking(false)
                .render(RenderLayer.CUTOUT)
                .register(TypeList.of(Void.class));
        VanillaProps.stone()
                .group(ModGroups.WATER_AND_AIR)
                .name("waterfall")
                .texture("block/8_topography/7_ice_snow/waterfall")
                .blocking(false)
                .register(TypeList.of(Void.class));

    }
}
