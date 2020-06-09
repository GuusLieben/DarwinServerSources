package com.darwinreforged.server.modules.server.unloader;

import com.darwinreforged.server.core.resources.ConfigSetting;
import com.darwinreforged.server.core.resources.translations.Translation;

@ConfigSetting("worldunloader")
public class WorldUnloaderTranslations {

    // TODO : Move to module?
    public static final Translation WU_ADDED = Translation.create("added", "$1Added $2{0} $1to the unload blacklist");
    public static final Translation WORLD_NOT_FOUND = Translation.create("error_not_found", "$4Could not find that world!");

    public WorldUnloaderTranslations() {
    }
}
