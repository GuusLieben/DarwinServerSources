package org.dockbox.selene.blockregistry.models;

import org.dockbox.selene.api.entity.annotations.Accessor;
import org.dockbox.selene.api.entity.annotations.Metadata;

import java.util.List;

@Metadata(alias = "blockid")
public class IdBinding
{
    @Accessor(getter = "getKey", setter = "setKey")
    private String key;

    @Accessor(getter = "getAliases", setter = "setAliases")
    private List<String> aliases;

    public IdBinding() { }

    public IdBinding(String key, List<String> aliases)
    {
        this.key = key;
        this.aliases = aliases;
    }

    public String getKey()
    {
        return this.key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public List<String> getAliases()
    {
        return this.aliases;
    }

    public void setAliases(List<String> aliases)
    {
        this.aliases = aliases;
    }
}
