package com.darwinreforged.server.core.files;

import com.darwinreforged.server.core.internal.Utility;
import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.modules.Module;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 The type File utils.
 */
@Utility("Common utilities for file management and parsing")
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class FileManager {

    private final ObjectMapper mapper;
    private static final Map<String, ConnectionSource> jdbcSources = new HashMap<>();
    private static final String JDBC_FORMAT = "jdbc:sqlite:%s";

    /**
     Instantiates a new File utils.
     */
    public FileManager() {
        YAMLFactory factory = new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER);
        mapper = new ObjectMapper(factory);
        mapper.findAndRegisterModules();
        mapper.setVisibility(PropertyAccessor.ALL, Visibility.ANY);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.WRAP_ROOT_VALUE);
        mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    }

    /**
     Gets data db.

     @param <T>
     the type parameter
     @param <I>
     the type parameter
     @param object
     the object
     @param file
     the file

     @return the data db
     */
    public <T, I> Dao<T, I> getDataDb(Class<T> object, File file) {
        if (isConnected(file)) {
            ConnectionSource source = jdbcSources.get(file.toString());
            try {
                TableUtils.createTableIfNotExists(source, object);
                return DaoManager.createDao(source, object);
            } catch (SQLException e) {
                DarwinServer.error("Could not create dao for object", e);
            }
        }
        return null;
    }

    /**
     Gets data db.

     @param <T>
     the type parameter
     @param <I>
     the type parameter
     @param object
     the object
     @param module
     the module

     @return the data db
     */
    public <T, I> Dao<T, I> getDataDb(Class<T> object, Object module) {
        Optional<Module> info;
        if (module instanceof Class) info = DarwinServer.getModMan().getModuleInfo((Class<?>) module);
        else info = DarwinServer.getModMan().getModuleInfo(module.getClass());

        if (info.isPresent()) {
            String id = info.get().id();
            File file = new File(getDataDirectory(module).toFile(), String.format("%s.dat", id));
            return getDataDb(object, file);
        }
        throw new RuntimeException("No such module registered");
    }

    /**
     Is connected boolean.

     @param file
     the file

     @return the boolean
     */
    public boolean isConnected(File file) {
        String fileAb = file.toString();
        if (jdbcSources.containsKey(fileAb) && jdbcSources.get(fileAb) != null) return true;
        String connStr = String.format(JDBC_FORMAT, fileAb);
        try {
            ConnectionSource connectionSource = new JdbcConnectionSource(connStr);
            jdbcSources.put(fileAb, connectionSource);
            return true;
        } catch (SQLException e) {
            DarwinServer.error(String.format("Failed to create connection source for '%s'", fileAb), e);
            return false;
        }
    }

    public Map<String, Object> getYamlDataForUrl(String url) {
        return getYamlDataForUrl(url, Map.class, new HashMap());
    }

    public <T> T getYamlDataForUrl(String url, Class<T> type, T defaultValue) {
        try {
            URL u = new URL(url);
            Yaml yaml = new Yaml();
            return yaml.loadAs(u.openStream(), type);
        } catch (IOException e) {
            DarwinServer.error(String.format("Failed to obtain YAML from url '%s'.", url));
            return defaultValue;
        }
    }

    /**
     Gets yaml data from file.

     @param <T>
     the type parameter
     @param file
     the file
     @param type
     the type
     @param defaultValue
     the default value

     @return the yaml data from file
     */
    /*
     * Config files (YAML)
     */
    public <T> T getYamlDataFromFile(File file, Class<T> type, T defaultValue) {
        if (!file.exists() || file.length() == 0) return defaultValue;
        try {
            T res = mapper.readValue(file, type);
            return res != null ? res : defaultValue;
        } catch (IOException e) {
            DarwinServer.error("Failed to get YAML data from file", e);
        }
        return defaultValue;
    }

    /**
     Gets yaml data from file.

     @param file
     the file

     @return the yaml data from file
     */
    public Map<String, Object> getYamlDataFromFile(File file) {
        return (Map<String, Object>) getYamlDataFromFile(file, Map.class, new HashMap());
    }

    /**
     Gets yaml data for config.

     @param module
     the module

     @return the yaml data for config
     */
    public Map<String, Object> getYamlDataForConfig(Object module) {
        return getYamlDataFromFile(getYamlConfigFile(module));
    }

    /**
     Gets yaml data for config.

     @param <T>
     the type parameter
     @param module
     the module
     @param path
     the path
     @param type
     the type

     @return the yaml data for config
     */
    public <T> T getYamlDataForConfig(Object module, String path, Class<T> type, T defaultValue) {
        Map<String, Object> values = getYamlDataForConfig(module);
        if (values.containsKey(path)) {
            Object val = values.get(path);
            if (val.getClass().isAssignableFrom(type) || val.getClass().equals(type))
                return (T) val;
        }

        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            return defaultValue;
        }
    }

    /**
     Gets yaml config file.

     @param module
     the module

     @return the yaml config file
     */
    public File getYamlConfigFile(Object module) {
        return getYamlConfigFile(module, true);
    }

    /**
     Gets yaml config file.

     @param module
     the module
     @param createIfNotExists
     the create if not exists

     @return the yaml config file
     */
    public File getYamlConfigFile(Object module, boolean createIfNotExists) {
        Path path = getConfigDirectory(module);
        Optional<Module> info;
        if (module instanceof Class) info = DarwinServer.getModMan().getModuleInfo((Class<?>) module);
        else info = DarwinServer.getModMan().getModuleInfo(module.getClass());

        if (info.isPresent()) {
            String moduleId = info.get().id();
            File file = new File(path.toFile(), String.format("%s.yml", moduleId));
            return createIfNotExists ? createFileIfNotExists(file) : file;
        }
        throw new RuntimeException("No such module registered");
    }

    /**
     Write yaml data to file.

     @param <T>
     the type parameter
     @param data
     the data
     @param file
     the file
     */
    public <T> void writeYamlDataToFile(T data, File file) {
        try {
            mapper.writeValue(createFileIfNotExists(file), data);
        } catch (IOException e) {
            DarwinServer.error("Failed to write YAML data to file", e);
        }
    }

    /**
     Write yaml data for config.

     @param data
     the data
     @param module
     the module
     */
    public void writeYamlDataForConfig(Map<String, Object> data, Object module) {
        writeYamlDataToFile(data, getYamlConfigFile(module));
    }


    /**
     Gets module directory.

     @return the module directory
     */
    /*
     * Default directories
     */
    public abstract Path getModuleDirectory();

    /**
     Gets config directory.

     @param module
     the module

     @return the config directory
     */
    public abstract Path getConfigDirectory(Object module);

    /**
     Gets log directory.

     @return the log directory
     */
    public abstract Path getLogDirectory();

    /**
     Gets data directory.

     @param module
     the module

     @return the data directory
     */
    public abstract Path getDataDirectory(Object module);

    /**
     Gets data directory.

     @param module
     the module
     @param dir
     the dir

     @return the data directory
     */
    public Path getDataDirectory(Object module, String dir) {
        Path ddir = getDataDirectory(module);
        return createPathIfNotExist(new File(ddir.toFile(), dir).toPath());
    }

    /**
     Create file if not exists file.

     @param file
     the file

     @return the file
     */
    /*
     * Path and file existence validation
     */
    public File createFileIfNotExists(File file) {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return file;
    }

    /**
     Create path if not exist path.

     @param path
     the path

     @return the path
     */
    protected Path createPathIfNotExist(Path path) {
        if (!path.toFile().exists()) path.toFile().mkdirs();
        return path;
    }
}
