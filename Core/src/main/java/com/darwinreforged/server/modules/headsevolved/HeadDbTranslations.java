package com.darwinreforged.server.modules.headsevolved;

import com.darwinreforged.server.core.resources.ConfigSetting;
import com.darwinreforged.server.core.resources.translations.Translation;

@ConfigSetting("headdatabase")
public class HeadDbTranslations {

    public static final Translation OPEN_GUI_ERROR = Translation.create("error_opening", "$4Failed to open Head Database GUI");
    // TODO : This should be a config setting
    public static final Translation HEADS_EVOLVED_API_URL = Translation.create("url", "https://minecraft-heads.com/scripts/api.php?tags=true&cat={0}");
    public static final Translation HEADS_EVOLVED_FAILED_LOAD = Translation.create("error_loading", "$4Failed to load {0} heads");

    public HeadDbTranslations() {
    }

}
