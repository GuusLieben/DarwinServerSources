package org.dockbox.selene.blockregistry.models;

import java.util.List;

public class BindingsModel
{
    private List<IdBinding> bindings;

    public BindingsModel(List<IdBinding> bindings)
    {
        this.bindings = bindings;
    }

    public List<IdBinding> getBindings()
    {
        return this.bindings;
    }

    public void setBindings(List<IdBinding> bindings)
    {
        this.bindings = bindings;
    }
}
