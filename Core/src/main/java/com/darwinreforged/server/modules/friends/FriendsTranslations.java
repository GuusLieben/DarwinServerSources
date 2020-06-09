package com.darwinreforged.server.modules.friends;

import com.darwinreforged.server.core.resources.ConfigSetting;
import com.darwinreforged.server.core.resources.translations.Translation;

@ConfigSetting("friends")
public class FriendsTranslations {

    public static final Translation ACCEPTING_TP = Translation.create("accepting_teleport", "$2Accept teleports from friends : $1{0}");
    public static final Translation FRIEND_TELEPORTED = Translation.create("friend_teleported_to", "$2{0} $1teleported to your location");
    public static final Translation NO_TP_NOT_FRIENDS = Translation.create("error_not_friends", "$4You are not friends with that user so you cannot teleport to them");
    public static final Translation ALREADY_FRIENDS = Translation.create("error_already_friends", "You are already friends with $2{0}");
    public static final Translation FRIEND_ADDED = Translation.create("friend_added", "You are now friends with $2{0}");
    public static final Translation REQUEST_SENT = Translation.create("friend_requested", "$1A friend request was sent to $2{0}");
    public static final Translation REQUEST_RECEIVED = Translation.create("friend_received", "$2{0} $1has requested to befriend you");
    public static final Translation FRIEND_REMOVED = Translation.create("friend_removed", "$3{0} $1was removed as friend");
    public static final Translation FRIEND_ACCEPT_BUTTON = Translation.create("btn_accept", "&f&mAccept");
    public static final Translation FRIEND_ACCEPT_BUTTON_HOVER = Translation.create("btn_accept_hover", "$1Click to accept request from {0}");
    public static final Translation FRIEND_DENY_BUTTON = Translation.create("btn_deny", "&f&mDeny");
    public static final Translation FRIEND_DENY_BUTTON_HOVER = Translation.create("btn_deny_hover", "$1Click to deny request from {0}");
    public static final Translation FRIEND_ROW_REQUEST = Translation.create("friend_request_row", "$1{0} $2- Request");
    public static final Translation FRIEND_LIST_TITLE = Translation.create("friend_list_header", "$1Friends");

    public FriendsTranslations() {
    }

}
