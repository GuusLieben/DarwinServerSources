package com.darwinreforged.server.core.player;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.types.living.Target;

import java.util.UUID;

public class DarwinUser extends Target {

    public DarwinUser(UUID uuid, String name) {
        super.uuid = uuid;
        super.name = name;
    }

    @Override
    public String getName(boolean lookup) {
        return name;
    }

    @Override
    public void execute(String cmd) {
        PlayerManager pm = DarwinServer.getUtilMan().get(PlayerManager.class);
        if (pm.isOnline(super.uuid)) pm.executeCmd(cmd, this);
        else throw new UnsupportedOperationException("Cannot perform command for offline user");
    }
}
