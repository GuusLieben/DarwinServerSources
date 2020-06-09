package com.darwinreforged.server.modules.gotolobby;

import com.darwinreforged.server.core.resources.ConfigSetting;
import com.darwinreforged.server.core.resources.translations.Translation;

@ConfigSetting("gotolobby")
public class GoToLobbyTranslations {

    // TODO : Move to module
    public static final Translation GTL_WARPED = Translation.create("warped", "$1You have been teleported to the lobby as the world you were previously in is disabled");

    public GoToLobbyTranslations() {
    }

}
