package com.darwinreforged.server.modules.todo.plottrustlimit;

import com.darwinreforged.server.core.resources.ConfigSetting;
import com.darwinreforged.server.core.resources.translations.Translation;

@ConfigSetting("trustlimit")
public class TrustLimitTranslations {

    // TODO : Move to module
    public static final Translation TRUST_LIMIT_AUTO_CLEANED = Translation.create("auto_cleaned", "$1Automatically removed $2{0} $1from this plot because their rank is too low.");

    public TrustLimitTranslations() {
    }
}
