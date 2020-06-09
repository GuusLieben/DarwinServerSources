package com.darwinreforged.server.modules.layerheight;

import com.darwinreforged.server.core.resources.ConfigSetting;
import com.darwinreforged.server.core.resources.translations.Translation;

@ConfigSetting("heighttool")
public class HeightToolTranslations {

    public static final Translation HEIGHT_TOO_HIGH = Translation.create("error_too_high", "$4Height cannot be above 8");
    public static final Translation HEIGHT_TOO_LOW = Translation.create("error_too_low", "$4Height cannot be below 1");
    public static final Translation HEIGHTTOOL_NAME = Translation.create("tool_name", "$1Layer Height Tool: $2{0}");
    public static final Translation HEIGHTTOOL_SET = Translation.create("tool_set", "Successfully set the layer height to $2{0}");
    public static final Translation HEIGHTTOOL_FAILED_BIND = Translation.create("error_failed_bind", "$4Tool cannot be bound to a block");

    public HeightToolTranslations() {
    }

}
