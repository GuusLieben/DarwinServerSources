package com.darwinreforged.server.modules.todo.plotidbar;

import com.darwinreforged.server.core.resources.ConfigSetting;
import com.darwinreforged.server.core.resources.translations.Translation;

@ConfigSetting("pidbar")
public class PlotIdBarTranslations {

    public static final Translation PID_USERS_TRUSTED_MORE = Translation.create("trusted_several", "{0}, {1} and {2} others");
    public static final Translation PID_USERS_TRUSTED = Translation.create("trusted_two", "{0}, {1}");
    public static final Translation PID_WORLD_FORMAT = Translation.create("id_world", "$2World ID : $1{0}");
    public static final Translation PID_PLOT_FORMAT = Translation.create("id_plot", "$2Plot ID : $1{0}, {1}");
    public static final Translation PID_OWNER_FORMAT = Translation.create("owner", "$2Owner : $1{0}");
    public static final Translation PID_BAR_SEPARATOR = Translation.create("separator", " &f|-=-| ");
    public static final Translation PID_BAR_MEMBERS = Translation.create("members", "$2Members : $1{0}");
    public static final Translation PID_TOGGLE_BAR = Translation.create("toggled_bar", "Updated PlotID Bar preference to $2{0}");
    public static final Translation PID_TOGGLE_MEMBERS = Translation.create("toggled_members", "Updated PlotID Members preference to $2{0}");

    public PlotIdBarTranslations() {
    }
}
