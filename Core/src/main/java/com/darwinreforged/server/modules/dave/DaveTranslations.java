package com.darwinreforged.server.modules.dave;

import com.darwinreforged.server.core.resources.ConfigSetting;
import com.darwinreforged.server.core.resources.translations.Translation;

@ConfigSetting("dave")
public class DaveTranslations {

    public static final Translation DAVE_LINK_SUGGESTION = Translation.create("link_suggestion", "Here's a useful link, $1{0}");
    public static final Translation DAVE_LINK_SUGGESTION_HOVER = Translation.create("link_suggestion_hover", "$2Click to open $1{0}");
    public static final Translation DAVE_DISCORD_FORMAT = Translation.create("discord_format", "**Dave** ≫ {0}");
    public static final Translation DAVE_MUTED = Translation.create("muted", "Muted Dave, note that important triggers will always show");
    public static final Translation DAVE_UNMUTED = Translation.create("unmuted", "Unmuted Dave");
    public static final Translation DAVE_RELOADED_USER = Translation.create("reloaded_user", "{0} &f: Reloaded Dave without breaking stuff, whoo!");
    public static final Translation DAVE_TRIGGER_LIST_ITEM = Translation.create("trigger_row", "$3 - $1{0}");
    public static final Translation DAVE_TRIGGER_HOVER = Translation.create("trigger_row_hover", "$1Click to perform trigger");

    public DaveTranslations() {
    }
}
