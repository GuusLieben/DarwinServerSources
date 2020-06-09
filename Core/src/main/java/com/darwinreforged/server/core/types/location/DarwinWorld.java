package com.darwinreforged.server.core.types.location;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.util.LocationUtils;

import java.util.UUID;

/**
 The type Darwin world.
 */
public class DarwinWorld {

    private final UUID worldUUID;
    private final String name;

    /**
     Instantiates a new Darwin world.

     @param worldUUID
     the world uuid
     @param name
     the name
     */
    public DarwinWorld(UUID worldUUID, String name) {
        this.worldUUID = worldUUID;
        this.name = name;
    }

    /**
     Gets world uuid.

     @return the world uuid
     */
    public UUID getWorldUUID() {
        return worldUUID;
    }

    /**
     Gets name.

     @return the name
     */
    public String getName() {
        return name;
    }

    /**
     Gets player count.

     @return the player count
     */
    public int getPlayerCount() {
        return DarwinServer.getUtilMan().get(LocationUtils.class).getPlayerCountInWorld(this);
    }

    /**
     Unload world.
     */
    public void unloadWorld() {
        unloadWorld(true);
    }

    /**
     Unload world.

     @param keepLoaded
     the keep loaded
     */
    public void unloadWorld(boolean keepLoaded) {
        DarwinServer.getUtilMan().get(LocationUtils.class).unloadWorld(this, keepLoaded);
    }
}
