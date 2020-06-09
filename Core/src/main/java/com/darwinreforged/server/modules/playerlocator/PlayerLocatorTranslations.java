package com.darwinreforged.server.modules.playerlocator;

import com.darwinreforged.server.core.resources.ConfigSetting;
import com.darwinreforged.server.core.resources.translations.Translation;

@ConfigSetting("locator")
public class PlayerLocatorTranslations {

    public static final Translation PLAYER_IN_WORLD = Translation.create("in_world", "$1{0} $2is in $1{1}");
    public static final Translation PLAYER_ON_ROAD = Translation.create("on_road", "$1{0} $2is on a road");
    public static final Translation PLAYER_IN_PLOT = Translation.create("in_lot", "$1{0} $2is in $1{1}, {2}");

    public PlayerLocatorTranslations() {
    }

}
