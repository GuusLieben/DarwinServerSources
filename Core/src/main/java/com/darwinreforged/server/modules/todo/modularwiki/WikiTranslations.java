package com.darwinreforged.server.modules.todo.modularwiki;

import com.darwinreforged.server.core.resources.ConfigSetting;
import com.darwinreforged.server.core.resources.translations.Translation;

@ConfigSetting("wiki")
public class WikiTranslations {

    public static final Translation WIKI_NOT_ALLOWED = Translation.create("error_not_allowed", "$4You do not have permission to view this wiki '{0}'");
    public static final Translation WIKI_NOT_FOUND = Translation.create("error_not_found", "No wiki entries were found for the requested value '{0}'");
    public static final Translation WIKI_LIST_ROW = Translation.create("row", " $3- $1{0} $2[View]");
    public static final Translation WIKI_SHARE_BUTTON = Translation.create("btn_share", "$2[Share $1'{0}'$2]");
    public static final Translation WIKI_SHARE_BUTTON_HOVER = Translation.create("btn_share_hover", "$1Share wiki with another player");
    public static final Translation WIKI_VIEW_BUTTON = Translation.create("btn_view", "$2[$1View$2]");
    public static final Translation WIKI_VIEW_BUTTON_HOVER = Translation.create("btn_view_hover", "$1View entry '{0}'");
    public static final Translation WIKI_LIST_ROW_HOVER = Translation.create("row_hover", "$1More information about {0}");
    public static final Translation WIKI_NO_ENTRIES = Translation.create("error_no_entries", "$1No wiki entries were found");
    public static final Translation WIKI_SHARED_USER = Translation.create("shared_from", "$2{0} $1shared the $2'{1}' $1entry with you");
    public static final Translation WIKI_SHARED_WITH = Translation.create("shared_with", "$2You shared the $1'{0}' $2entry with $1{1}");
    public static final Translation WIKI_OPEN_ENTRY_HOVER = Translation.create("action_open_hover", "Open entry '{0}'");

    public WikiTranslations() {
    }
}
