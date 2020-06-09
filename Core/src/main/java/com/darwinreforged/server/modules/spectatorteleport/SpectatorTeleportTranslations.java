package com.darwinreforged.server.modules.spectatorteleport;

import com.darwinreforged.server.core.resources.ConfigSetting;
import com.darwinreforged.server.core.resources.translations.Translation;

@ConfigSetting("spectatortp")
public class SpectatorTeleportTranslations {

    public static final Translation SPECTATOR_TP_DISALLOWED = Translation.create("error_not_allowed", "$3You are not allowed to teleport while in spectator mode");

    public SpectatorTeleportTranslations() {
    }
}
