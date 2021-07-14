package org.dockbox.hartshorn.blockregistry;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.test.HartshornRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HartshornRunner.class)
public class BlockRegistryTests
{
    private static BlockRegistryManager blockRegistryManager;

    //Only for use of initial construction of block registry.
    @Test
    @Disabled
    public void constructBlockRegistry() {
        BlockDataInitialisationManager.constructBlockRegistry();
    }

    @BeforeAll
    public static void setupBlockRegistry() {
        blockRegistryManager = Hartshorn.context().get(BlockRegistryManager.class);
    }

    @Test
    public void familyIdTest() {
        Exceptional<String> familyId1 = blockRegistryManager.familyId("conquest:blue_schist_vertical_slab");
        Exceptional<String> familyId2 = blockRegistryManager.familyId("conquest:blue_schist");
        Exceptional<String> notPresentFamilyId = blockRegistryManager.familyId("blue");

        Assertions.assertTrue(familyId1.present());
        Assertions.assertEquals("conquest:blue_schist", familyId1.get());

        Assertions.assertTrue(familyId2.present());
        Assertions.assertEquals("conquest:blue_schist", familyId2.get());

        Assertions.assertTrue(notPresentFamilyId.absent());
    }

    @Test
    public void determineVariantTest() {
        Exceptional<VariantIdentifier> verticalSlabVariant = blockRegistryManager
            .variant("conquest:blue_schist_vertical_slab");
        Exceptional<VariantIdentifier> fullBlockVariant = blockRegistryManager
            .variant("conquest:blue_schist");
        Exceptional<VariantIdentifier> notPresentVariant = blockRegistryManager.variant("blue");

        Assertions.assertTrue(verticalSlabVariant.present());
        Assertions.assertEquals(VariantIdentifier.VERTICAL_SLAB, verticalSlabVariant.get());

        Assertions.assertTrue(fullBlockVariant.present());
        Assertions.assertEquals(VariantIdentifier.FULL, fullBlockVariant.get());

        Assertions.assertTrue(notPresentVariant.absent());
    }

    @Test
    public void addVariantTest() {
        blockRegistryManager.addVariant("conquest:block", VariantIdentifier.SLAB, "conquest:block_slab");

        Exceptional<VariantIdentifier> slabVariant = blockRegistryManager.variant("conquest:block_slab");
        Exceptional<VariantIdentifier> notPresentVariant = blockRegistryManager.variant("conquest:block_stairs");

        Assertions.assertTrue(slabVariant.present());
        Assertions.assertEquals(VariantIdentifier.SLAB, slabVariant.get());

        Assertions.assertTrue(notPresentVariant.absent());
    }

    @Test
    public void addAliasTest() {
        blockRegistryManager.addAlias("conquest:lime_mortar_masonry", "conquest:plastered_stone");

        final Item original = Item.of("conquest:lime_mortar_masonry");
        final Item item = Item.of("conquest:plastered_stone");
        System.out.println(item.id());
        Exceptional<VariantIdentifier> variant = blockRegistryManager.variant(item);

        Assertions.assertNotNull(item);
        Assertions.assertTrue(variant.present());
        Assertions.assertEquals(VariantIdentifier.FULL, variant.get());
    }
}
