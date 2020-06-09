package com.darwinreforged.server.modules.player.moves;

import com.darwinreforged.server.core.resources.ConfigSetting;
import com.darwinreforged.server.core.resources.translations.Translation;

@ConfigSetting("gotolobby")
public class PlayerMoveActionTranslations {

    public static final Translation GTL_WARPED = Translation.create("warped", "$1You have been teleported to the lobby as the world you were previously in is disabled");
    public static final Translation WD_NOT_PERMITTED = Translation.create("error_not_permitted", "$4You are not allowed to teleport to that world as you are denied from it!");
    public static final Translation SPECTATOR_TP_DISALLOWED = Translation.create("error_not_allowed", "$3You are not allowed to teleport while in spectator mode");

    public PlayerMoveActionTranslations() {
    }

}
