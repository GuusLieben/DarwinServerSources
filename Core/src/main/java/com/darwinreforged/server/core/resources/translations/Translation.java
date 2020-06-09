package com.darwinreforged.server.core.resources.translations;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.files.FileManager;
import com.darwinreforged.server.core.modules.Module;
import com.darwinreforged.server.core.resources.ConfigSetting;
import com.darwinreforged.server.core.util.CommonUtils;
import com.darwinreforged.server.modules.DefaultModule;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.reflections.Reflections;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.darwinreforged.server.core.resources.translations.DefaultTranslations.COLOR_ERROR;
import static com.darwinreforged.server.core.resources.translations.DefaultTranslations.COLOR_MINOR;
import static com.darwinreforged.server.core.resources.translations.DefaultTranslations.COLOR_PRIMARY;
import static com.darwinreforged.server.core.resources.translations.DefaultTranslations.COLOR_SECONDARY;

public class Translation {

    private String value;
    private String key;
    private String category;

    public void setValue(String value) {
        this.value = value;
    }

    public Translation() {
    }

    Translation(String value, String key) {
        this.value = value;
        this.key = key;
    }

    private String getValue() {
        // Prefer cached translations (loaded from configuration) over default values
        if (category != null && TRANSLATION_STORAGE.containsKey(category) && TRANSLATION_STORAGE.get(category).containsKey(key))
            return TRANSLATION_STORAGE.get(category).get(key);
        return value;
    }

    public String getKey() {
        return key;
    }

    // String
    public String s() {
        return parseColors(getValue());
    }

    @JsonIgnore
    public static final Map<String, Map<String, String>> TRANSLATION_STORAGE = new HashMap<>();

    // Unparsed
    @JsonIgnore
    public String u() {
        return getValue().replaceAll("[$|&][0-9a-fklmnor]", "");
    }

    // Formatted
    @JsonIgnore
    public String f(final Object... args) {
        return format(getValue(), args);
    }

    // Formatted, shortened
    @JsonIgnore
    public String fsh(final Object... args) {
        int diff = s().length() - u().length();
        String formatted = f(args);
        return (49 + diff > formatted.length() ? formatted : f(args).substring(0, (50 + diff))) + "...";
    }

    @JsonIgnore
    public static String shorten(String s) {
        if (s.length() > 52) // Adding ... to a string of 50 would only make it longer
            return s.substring(0, 50) + "...";
        else return s;
    }

    // Format value placeholders and colors
    @JsonIgnore
    public static String format(String m, Object... args) {
        if (args.length == 0) return m;
        Map<String, String> map = new LinkedHashMap<>();
        if (args.length > 0) {
            for (int i = args.length - 1; i >= 0; i--) {
                String arg = "" + args[i];
                if (arg == null || arg.isEmpty()) map.put(String.format("{%d}", i), "empty");
                else map.put(String.format("{%d}", i), arg);
            }
        }
        m = CommonUtils.replaceFromMap(m, map);
        return parseColors(m);
    }

    @JsonIgnore
    public static String parseColors(String m) {
        char[] nativeFormats = "abcdef1234567890klmnor".toCharArray();
        for (char c : nativeFormats) m = m.replaceAll(String.format("&%s", c), String.format("\u00A7%s", c));

        return m
                .replaceAll("\\$1", String.format("\u00A7%s", COLOR_PRIMARY.getValue()))
                .replaceAll("\\$2", String.format("\u00A7%s", COLOR_SECONDARY.getValue()))
                .replaceAll("\\$3", String.format("\u00A7%s", COLOR_MINOR.getValue()))
                .replaceAll("\\$4", String.format("\u00A7%s", COLOR_ERROR.getValue()));
    }

    // Change
    @JsonIgnore
    private void c(String s) {
        this.value = s;
    }

    @JsonIgnore
    public static Translation create(String key, String translation) {
        try {
            // Obtain category
            StackTraceElement[] ste = Thread.currentThread().getStackTrace();
            // 0: Thread, 1: Translations, 2: Caller
            String caller = ste[2].getClassName();
            Class<?> callerClass = Class.forName(caller);
            String category = "other";
            if (callerClass.isAnnotationPresent(ConfigSetting.class)) {
                ConfigSetting cs = callerClass.getAnnotation(ConfigSetting.class);
                category = cs.value();
            } else if (callerClass.isAnnotationPresent(Module.class)) {
                Module mod = callerClass.getAnnotation(Module.class);
                category = mod.id();
            }

            if (key != null) {
                DarwinServer.getLog().info(String.format("Registered '%s.%s' for translation '%s'", category, key, translation));
                Translation t = new Translation(translation, key);
                t.category = category;

                Translation.TRANSLATION_STORAGE.putIfAbsent(category, new HashMap<>());
                Map<String, String> categoryTranslations = Translation.TRANSLATION_STORAGE.get(category);
                categoryTranslations.put(t.key, t.value);
                Translation.TRANSLATION_STORAGE.put(category, categoryTranslations);
                return t;
            } else System.out.println("Key null");
        } catch (Throwable e) {
            DarwinServer.error("Failed to register translation", e);
        }
        DarwinServer.getLog().warn("Returning non-stored translation");
        return new Translation(translation, "empty");
    }

    @JsonIgnore
    public static void initTranslationService() {
        FileManager fm = DarwinServer.getUtilMan().get(FileManager.class);
        if (!getStorageFile(fm).exists()) {
            Reflections ref = new Reflections(Translation.class.getPackage().getName());
            Set<Class<?>> bulkTranslationTypes = ref.getTypesAnnotatedWith(ConfigSetting.class);
            bulkTranslationTypes.forEach(type -> {
                try {
                    Constructor<?> ctor = type.getConstructor();
                    ctor.setAccessible(true);
                    ctor.newInstance();
                } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    DarwinServer.error(String.format("Could not safely instantiate bulk translation type '%s'. Note that this may cause incomplete configurations", type.toGenericString()), e);
                }
            });
            fm.writeYamlDataToFile(Translation.TRANSLATION_STORAGE, getStorageFile(fm));
        } else readFromFile();
    }

    @SuppressWarnings("unchecked")
    public static void readFromFile() {
        FileManager fm = DarwinServer.getUtilMan().get(FileManager.class);
        Map<String, Object> storedTranslations = fm.getYamlDataFromFile(getStorageFile(fm));
        storedTranslations.forEach((category, map) -> {
            if (!(map instanceof Map))
                DarwinServer.getLog().warn(String.format("Attempted to read non-map value from translations '%s'", category));
            else TRANSLATION_STORAGE.put(category, (Map<String, String>) map);
        });
    }

    private static File getStorageFile(FileManager fm) {
        Path dataDir = fm.getDataDirectory(DefaultModule.class, "translations");
        return new File(dataDir.toFile(), "translations.yml");
    }


}
