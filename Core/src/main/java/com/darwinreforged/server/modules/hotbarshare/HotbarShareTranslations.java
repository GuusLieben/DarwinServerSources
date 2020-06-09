package com.darwinreforged.server.modules.hotbarshare;

import com.darwinreforged.server.core.resources.ConfigSetting;
import com.darwinreforged.server.core.resources.translations.Translation;

@ConfigSetting("hotbar")
public class HotbarShareTranslations {

    public static final Translation SHARED_HOTBAR_WITH = Translation.create("shared_with", "You shared your hotbar with {0}");
    public static final Translation PLAYER_SHARED_HOTBAR = Translation.create("shared_from", "$2{0} $1shared their hotbar");
    public static final Translation FULL_HOTBAR = Translation.create("error_full", "$4Your hotbar is full, please clear one or more slots");
    public static final Translation HOTBAR_SHARE_HEADER = Translation.create("info_header", "$1&m------- &r$2{0}'s Hotbar $1&m-------");
    public static final Translation HOTBAR_SHARE_INDEX = Translation.create("info_index", "$2#{0} : $1{1}");
    public static final Translation HOTBAR_SHARE_ENCHANTED = Translation.create("info_enchanted", "$2  Enchanted : ");
    public static final Translation HOTBAR_SHARE_LORE = Translation.create("info_lore", "$2  Lore : ");
    public static final Translation HOTBAR_VIEW_BUTTON = Translation.create("btn_view", "$2[$1View$2]");
    public static final Translation HOTBAR_VIEW_BUTTON_HOVER = Translation.create("btn_view_hover", "View hotbar");
    public static final Translation HOTBAR_LOAD_BUTTON = Translation.create("btn_load", "$2[$1Load$2]");
    public static final Translation HOTBAR_LOAD_BUTTON_HOVER = Translation.create("btn_load_hover", "Load hotbar");

    public HotbarShareTranslations() {
    }
}
