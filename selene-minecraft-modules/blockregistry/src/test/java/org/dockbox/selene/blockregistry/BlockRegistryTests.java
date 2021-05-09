package org.dockbox.selene.blockregistry;

import org.dockbox.selene.api.domain.tuple.Tuple;
import org.dockbox.selene.blockregistry.handlers.IBlockRegistryHandler;
import org.dockbox.selene.blockregistry.handlers.MC1_12BlockRegistryHandler;
import org.dockbox.selene.server.minecraft.item.Item;
import org.dockbox.selene.server.minecraft.item.storage.MinecraftItems;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BlockRegistryTests
{
    private static final IBlockRegistryHandler handler = new MC1_12BlockRegistryHandler();

    @Test
    public void getRoot() {
        Assertions.assertEquals(
            Tuple.of("conquest:stone_full_1",0),
            handler.getRoot("conquest:stone_stairs_1:0"));
    }

    @Test
    public void registerCustomItem() {
        MinecraftItems.getInstance().registerCustom("stone_plastered_full", () -> Item.of("conquest:stone_full_1", 0));
        Item item = Item.of("stone_plastered_full");
        System.out.println(item.getId());
    }
}
