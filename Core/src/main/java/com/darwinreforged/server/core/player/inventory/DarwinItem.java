package com.darwinreforged.server.core.player.inventory;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.util.ItemUtils;

/**
 The type Darwin item.

 @param <I>
 the type parameter
 */
@SuppressWarnings("unchecked")
public class DarwinItem<I> {

    /**
     The Item reference.
     */
    I itemReference;

    /**
     Instantiates a new Darwin item.

     @param itemReference
     the item reference
     */
    public DarwinItem(I itemReference) {
        this.itemReference = itemReference;
    }

    /**
     Sets lore.

     @param lore
     the lore
     */
    public void setLore(String[] lore) {
        this.itemReference = (I) DarwinServer.getUtilMan().get(ItemUtils.class).setLore(lore, this);
    }

    /**
     Sets display name.

     @param displayName
     the display name
     */
    public void setDisplayName(String displayName) {
        this.itemReference = (I) DarwinServer.getUtilMan().get(ItemUtils.class).setDisplayName(displayName, this);
    }

    /**
     Gets display name.

     @return the display name
     */
    public String getDisplayName() {
        return DarwinServer.getUtilMan().get(ItemUtils.class).getDisplayName(this);
    }

    /**
     Gets item reference.

     @return the item reference
     */
    public I getItemReference() {
        return itemReference;
    }

    /**
     Sets item reference.

     @param itemReference
     the item reference
     */
    public void setItemReference(I itemReference) {
        this.itemReference = itemReference;
    }
}
