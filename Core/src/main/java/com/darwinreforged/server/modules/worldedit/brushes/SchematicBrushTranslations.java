package com.darwinreforged.server.modules.worldedit.brushes;

import com.darwinreforged.server.core.resources.ConfigSetting;
import com.darwinreforged.server.core.resources.translations.Translation;

@ConfigSetting("schematicbrush")
public class SchematicBrushTranslations {

    public static final Translation SCHEMATIC_EMPTY = Translation.create("error_empty", "Schematic is empty");
    public static final Translation SCHEMATIC_APPLIED_CLIPBOARD = Translation.create("applied_clipboard", "Applied '{0}', flip={1}, rot={2}, place={3}");
    public static final Translation SCHEMATIC_SET_NOT_ALLOWED = Translation.create("error_not_allowed", "Not permitted to use schematic sets");
    public static final Translation SCHEMATIC_SET_NOT_FOUND = Translation.create("error_not_found", "Schematic set '{0}' not found");
    public static final Translation SCHEMATIC_INVALID_DEFINITION = Translation.create("error_invalid_def", "Invalid schematic definition: {0}");
    public static final Translation SCHEMATIC_INVALID_FILENAME = Translation.create("error_invalid_file", "Invalid filename pattern: {0} - {1}");
    public static final Translation SCHEMATIC_BAD_OFFSET_Y = Translation.create("error_bad_offset", "Bad y-offset value: {0}");
    public static final Translation SCHEMATIC_BAD_PLACE_CENTER = Translation.create("error_bad_place", "Bad place value ({0}) - using CENTER");
    public static final Translation SCHEMATIC_BRUSH_SET = Translation.create("brush_set", "Schematic brush set");
    public static final Translation COULD_NOT_DETECT_WORLDEDIT = Translation.create("error_no_worldedit", "Could not detect a supported version of WorldEdit");
    public static final Translation SCHEMATIC_PATTERN_REQUIRED = Translation.create("error_no_pattern", "Schematic brush requires &set-id or one or more schematic patterns");
    public static final Translation SCHEMATIC_SET_LIST_ROW = Translation.create("list_row", "{0}: desc='{1}'");
    public static final Translation SCHEMATIC_SET_LIST_COUNT = Translation.create("list_count", "{0} sets returned");
    public static final Translation SCHEMATIC_SET_ID_MISSING = Translation.create("error_missing_id", "Missing set ID");
    public static final Translation SCHEMATIC_SET_ALREADY_DEFINED = Translation.create("error_set_defined", "Set '{0}' already defined");
    public static final Translation SCHEMATIC_SET_NOT_DEFINED = Translation.create("error_set_not_defined", "Set '{0}' not defined");
    public static final Translation SCHEMATIC_SET_INVALID = Translation.create("error_set_invalid", "Schematic '{0}' invalid - ignored");
    public static final Translation SCHEMATIC_SET_CREATED = Translation.create("set_created", "Set '{0}' created");
    public static final Translation SCHEMATIC_SET_DELETED = Translation.create("set_deleted", "Set '{0}' deleted");
    public static final Translation SCHEMATIC_SET_UPDATED = Translation.create("set_updated", "Set '{0}' updated");
    public static final Translation SCHEMATIC_REMOVED_SET = Translation.create("set_removed", "Schematic '{0}' removed");
    public static final Translation SCHEMATIC_NOT_FOUND_SET = Translation.create("error_set_not_found", "Schematic '{0}' not found in set");
    public static final Translation SCHEMATIC_SET_DESCRIPTION = Translation.create("set_description", "Description: {0}");
    public static final Translation SCHEMATIC_DESCRIPTION = Translation.create("schematic_description", "Schematic: {0} ({1})");
    public static final Translation SCHEMATIC_SET_WEIGHT_TOO_HIGH = Translation.create("error_set_weight_too_high", "Warning: total weights exceed 100 - schematics without weights will never be selected");
    public static final Translation SCHEMATIC_INVALID_FORMAT = Translation.create("error_invalid_format", "Invalid format: {0}");
    public static final Translation SCHEMATIC_LIST_PAGINATION_FOOTER = Translation.create("schematic_list_footer", "Page {0} of {1} ({2} files)");
    public static final Translation SCHEMATIC_FILE_NOT_FOUND = Translation.create("error_file_not_found", "Schematic '{0}' file not found");
    public static final Translation SCHEMATIC_FORMAT_NOT_FOUND = Translation.create("error_format_not_found", "Schematic '{0}' format not found");
    public static final Translation SCHEMATIC_READ_ERROR = Translation.create("error_read", "$1Error reading schematic '{0}' - {1}");

    public SchematicBrushTranslations() {
    }
}
