package org.dockbox.hartshorn.blockregistry;

import org.dockbox.hartshorn.blockregistry.init.VanillaProps.ModGroups;
import org.dockbox.hartshorn.blockregistry.init.VanillaProps.RenderLayer;
import org.dockbox.hartshorn.blockregistry.init.VanillaProps.SoundType;
import org.dockbox.hartshorn.blockregistry.init.VanillaProps.TypeList;

import java.util.List;

public class BlockDataBuilder
{
    private String family;
    private String fullName;
    private String variantName;

    public BlockDataBuilder() {}

    public BlockDataBuilder group(ModGroups group)
    {
        return this;
    }

    public BlockDataBuilder family(String family)
    {
        this.family = family.startsWith("minecraft:") ? family : "conquest:" + family;
        return this;
    }

    public BlockDataBuilder name(String name)
    {
        this.fullName = name.startsWith("minecraft:") ? name : "conquest:" + name;
        this.variantName = name.startsWith("minecraft:") ? name.replace("minecraft:", "conquest:") : "conquest:" + name;;
        return this;
    }

    public BlockDataBuilder name(String name, String name2)
    {
        this.fullName = name.startsWith("minecraft:") ? name : "conquest:" + name;
        this.variantName = name2.startsWith("minecraft:") ? name2 : "conquest:" + name2;
        return this;
    }

    public BlockDataBuilder sound(SoundType sound)
    {
        return this;
    }

    public BlockDataBuilder texture(String texture)
    {
        return this;
    }

    public BlockDataBuilder texture(String name, String texture)
    {
        return this;
    }

    public BlockDataBuilder blocking(boolean blocking)
    {
        return this;
    }

    public BlockDataBuilder solid(boolean isSolid)
    {
        return this;
    }

    public BlockDataBuilder manual()
    {
        return this;
    }

    public BlockDataBuilder waterColor()
    {
        return this;
    }

    public BlockDataBuilder grassColor()
    {
        return this;
    }

    public BlockDataBuilder foliageColor()
    {
        return this;
    }

    public BlockDataBuilder render(RenderLayer name)
    {
        return this;
    }

    public void register(TypeList type)
    {
        if (null == this.family || this.family.isEmpty())
            this.family = this.fullName;
        List<VariantIdentifier> variants = type.variantIdentifiers();

        if (variants.isEmpty()) {
            VariantIdentifier variant = this.identifyVariant(this.fullName);
            BlockDataInitialisationManager.BlockRegistryManager
                .addVariant(this.family, variant, this.variantName);
        }
        else {
            for (VariantIdentifier variant : variants) {
                String variantId;

                if (VariantIdentifier.FULL == variant)
                    variantId = this.fullName;
                else {
                    variantId = this.variantName + '_' + variant.getIdentifier();
                }

                BlockDataInitialisationManager.BlockRegistryManager
                    .addVariant(this.family, variant, variantId);
            }
        }

    }

    private VariantIdentifier identifyVariant(String name)
    {
        for (VariantIdentifier variantIdentifier : VariantIdentifier.values()) {
            if (name.endsWith(variantIdentifier.getIdentifier()))
                return variantIdentifier;
        }

        return VariantIdentifier.FULL;
    }

    public BlockDataBuilder randomTick(boolean tick)
    {
        return this;
    }

    public BlockDataBuilder strength(double a, double b)
    {
        return this;
    }
}
