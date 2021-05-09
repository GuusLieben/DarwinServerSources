package org.dockbox.selene.blockregistry.handlers;

import org.dockbox.selene.api.domain.tuple.Tuple;
import org.dockbox.selene.server.minecraft.item.Item;

import java.util.List;

public interface IBlockRegistryHandler
{
    void loadIdRegistry();

    String getParent(String alias);

    String getRootString(String alias);

    Tuple<String, Integer> getRoot(String alias);

    List<String> getAliases(Item item);

    List<String> getAliases(String alias);

    List<String> getAliases(String parent, int meta);
}
