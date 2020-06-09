package com.darwinreforged.server.modules.paintings;

import com.darwinreforged.server.core.resources.ConfigSetting;
import com.darwinreforged.server.core.resources.translations.Translation;

@ConfigSetting("paintings")
public class PaintingTranslations {

    public static final Translation PNG_URL_REQUIRED = Translation.create("error_png_required", "$4URLs have to end with .png, please make sure to upload an image rather than a webpage including it");
    public static final Translation PAINTING_TOO_BIG = Translation.create("error_too_big", "$4Paintings can have a maximum size of 3x3");
    public static final Translation PAINTING_EXEMPT = Translation.create("exempt", "You are exempt from needing permission, check your list of paintings");
    public static final Translation PAINTING_SUBMITTED = Translation.create("submitted", "Submitted a new request, once accepted the painting will be automatically uploaded");
    public static final Translation PAINTING_STATUS_SUBMITTED = Translation.create("status_submitted", "Submitted");
    public static final Translation PAINTING_STATUS_REJECTED = Translation.create("status_rejected", "Rejected");
    public static final Translation PAINTING_STATUS_APPROVED = Translation.create("status_approved", "Approved");
    public static final Translation PAINTING_DISCORD_TITLE = Translation.create("discord_title", "Submissions");
    public static final Translation PAINTING_DISCORD_FIELD_TITLE = Translation.create("discord_id", "Submission ID : #{0}");
    public static final Translation PAINTING_DISCORD_FIELD_VALUE = Translation.create("discord_info", "Submitted by : {0}\nStatus : {1}");
    public static final Translation PAINTING_SUBMISSION_NOT_FOUND = Translation.create("error_not_found", "Cannot find submission : {0}");
    public static final Translation PAINTING_SUBMISSION_LIST = Translation.create("discord_list", "Submission IDs : {0}");
    public static final Translation PAINTING_APPROVING = Translation.create("discord_approving", "Approving Submission : {0}");
    public static final Translation PAINTING_REJECTING = Translation.create("discord_rejecting", "Rejecting Submission : {0}");
    public static final Translation PAINTING_CANNOT_UPDATE_STATUS = Translation.create("error_failed_update", "Cannot {0} this submission (#{1}). Are you sure it exists?");
    public static final Translation PAINTING_NEW_SUBMISSION_TITLE = Translation.create("discord_new", "New submission : #{0}");
    public static final Translation PAINTING_NEW_EXEMPT_SUBMISSION_TITLE = Translation.create("discord_new_exempt", "New exempt submission : #{0}");
    public static final Translation PAINTING_SUBMISSION_AUTHOR = Translation.create("discord_submitter", "Submitted by : {0}");
    public static final Translation PAINTING_SUBMISSION_SIZE_TITLE = Translation.create("discord_size_title", "Size");
    public static final Translation PAINTING_SUBMISSION_SIZE_VALUE = Translation.create("discord_size_value", "X: {0}\nY: {1}");

    public PaintingTranslations() {
    }
}
