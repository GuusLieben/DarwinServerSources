package com.darwinreforged.server.core.resources.translations;

import com.darwinreforged.server.core.resources.ConfigSetting;

@ConfigSetting("default")
public class DefaultTranslations {

    public static final Translation PREFIX = Translation.create("prefix", "$3[] $1");
    public static final Translation DEFAULT_SINGLE_MESSAGE = Translation.create("single_message", "$3[] $1{0}");
    public static final Translation COLOR_PRIMARY = Translation.create("color_primary", "b");
    public static final Translation COLOR_SECONDARY = Translation.create("color_secondary", "3");
    public static final Translation COLOR_MINOR = Translation.create("color_minor", "7");
    public static final Translation COLOR_ERROR = Translation.create("color_error", "c");
    public static final Translation PLAYER_ONLY_COMMAND = Translation.create("error_player_only", "$4This command can only be executed by players");
    public static final Translation COMMAND_NO_PERMISSION = Translation.create("error_no_permission", "$4You do not have permission to use this command $3({0})");
    public static final Translation PLOTS1_NAME = Translation.create("world_plots1", "Plots1");
    public static final Translation PLOTS2_NAME = Translation.create("world_plots2", "Plots2");
    public static final Translation PLOTS500_NAME = Translation.create("world_plots500", "Plots500");
    public static final Translation MASTERPLOTS_NAME = Translation.create("world_masterplots", "MasterPlots");
    public static final Translation MEMBER_RANK_DISPLAY = Translation.create("rank_member", "Member");
    public static final Translation EXPERT_RANK_DISPLAY = Translation.create("rank_exprt", "Expert");
    public static final Translation MASTER_ARCHITECTURE_DISPLAY = Translation.create("rank_master_architecture", "Mastered Skill Architecture");
    public static final Translation MASTER_NATURE_DISPLAY = Translation.create("rank_master_nature", "Mastered Skill Nature");
    public static final Translation MASTER_BOTH_DISPLAY = Translation.create("rank_master_both", "both Mastered Skills");
    public static final Translation MASTER_RANK_DISPLAY = Translation.create("rank_master", "Master");
    public static final Translation DEFAULT_SEPARATOR = Translation.create("separator", " - ");
    public static final Translation DEFAULT_PADDING = Translation.create("padding", " $1- ");
    public static final Translation DEFAULT_ON = Translation.create("on", "On");
    public static final Translation DEFAULT_OFF = Translation.create("off", "Off");
    public static final Translation UNKNOWN = Translation.create("unknown", "Unknown");
    public static final Translation NONE = Translation.create("none", "None");
    public static final Translation CONSOLE = Translation.create("console", "Console");
    public static final Translation ONLINE_PLAYER = Translation.create("online_player_displayname", "$1{0}");
    public static final Translation OFFLINE_PLAYER = Translation.create("offline_player_displayname", "$2{0}");
    public static final Translation UNOWNED = Translation.create("unowned", "Unowned");
    public static final Translation EVERYONE = Translation.create("everyone", "Everyone");
    public static final Translation UNKNOWN_PLAYER = Translation.create("error_unknown_player", "Unknown player");
    public static final Translation ARGUMENT_NOT_PROVIDED = Translation.create("error_missing_argument", "$4No argument provided for value '{0}'");
    public static final Translation PLAYER_NOT_FOUND = Translation.create("error_player_not_found", "$4Could not find player '{0}'");
    public static final Translation DEFAULT_TITLE = Translation.create("title", "$1{0}");
    public static final Translation UNKNOWN_ERROR = Translation.create("error_other", "$1An error occurred. {0}");
    public static final Translation COMMAND_HELP_COMMENT = Translation.create("command_help_comment", "$2{0} $3- $1{1}");
    public static final Translation COMMAND_HELP_COMMENT_ARGS = Translation.create("command_help_comment_with_args", "$2{0} $1{1} $3- $1{2}");
    public static final Translation OUTSIDE_PLOT = Translation.create("outside_plot", "$4You are not standing inside a plot");
    public static final Translation CMD_USAGE_TITLE = Translation.create("cmd_usage_title", "$3$2Usage for $1{0}");
    public static final Translation CMD_USAGE = Translation.create("cmd_usage", "$3- $1/{0}");
    public static final Translation CMD_FLAGS = Translation.create("cmd_flags", "$3- $2Flags: $1{0}");
    public static final Translation CMD_DESCRIPTION = Translation.create("cmd_description", "$3- $2Summary: $1{0}");
    public static final Translation DISABLED_MODULE_ROW = Translation.create("module_row_disabled", "$2 - &7[Disabled] $3{0} $3- $2{1} {2}");
    public static final Translation FAILED_MODULE_ROW = Translation.create("module_row_failed", "$2 - $4[Failed] {0}");
    public static final Translation ACTIVE_MODULE_ROW = Translation.create("module_row_active", "$2 - &a[Loaded] $1{0} $3- $2{1} {2}");
    public static final Translation DARWIN_MODULE_TITLE = Translation.create("module_info_title", "$1Darwin Server Info");
    public static final Translation DARWIN_MODULE_PADDING = Translation.create("padding", "&m$2=");
    public static final Translation DARWIN_SERVER_VERSION = Translation.create("dserver_version", "$2&lDarwin Server &r$3($1Version$3: $1{0} $3- $2Platform:{1}$3)");
    public static final Translation DARWIN_SERVER_UPDATE = Translation.create("dserver_last_update", "$2&lLast updated&r$3: $1{0}");
    public static final Translation DARWIN_SERVER_AUTHOR = Translation.create("dserver_authors", "$2&lAuthor&r$3: $1{0}");
    public static final Translation DARWIN_SERVER_MODULE_HEAD = Translation.create("dserver_modules_header", "$2&lModules&r$3:");
    public static final Translation MODULE_SOURCE = Translation.create("module_source", "&e[{0}]");
    public static final Translation DARWIN_SINGLE_MODULE_HEADER = Translation.create("module_single_header", "$2About $1{0}");
    public static final Translation DARWIN_SINGLE_MODULE_DATA = Translation.create("module_single_data", "$2ID : $1{0}\n" +
            "$2Name : $1{1}\n" +
            "$2Description : $1{2}\n" +
            "$2Version : $1{3}\n" +
            "$2URL : $1{4}\n" +
            "$2Dependencies : $1{5}\n" +
            "$2Author(s) : $1{6}\n" +
            "$2Source : $1{7}");
    public static final Translation DARWIN_SINGLE_MODULE_DEPENDENCY = Translation.create("module_single_dependency", "$1{0} $2({1} $3- {2}$2)");
    public static final Translation DARWIN_SERVER_MODULE_HOVER = Translation.create("module_row_hover", "$1More information for '{0}'");

    public DefaultTranslations() {
    }

}
