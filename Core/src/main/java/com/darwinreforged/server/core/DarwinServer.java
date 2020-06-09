package com.darwinreforged.server.core;

import com.darwinreforged.server.core.chat.DiscordChatManager;
import com.darwinreforged.server.core.commands.CommandBus;
import com.darwinreforged.server.core.events.util.EventBus;
import com.darwinreforged.server.core.files.FileManager;
import com.darwinreforged.server.core.internal.DarwinConfig;
import com.darwinreforged.server.core.internal.ServerType;
import com.darwinreforged.server.core.internal.Utility;
import com.darwinreforged.server.core.managers.LibraryManager;
import com.darwinreforged.server.core.managers.ModuleManager;
import com.darwinreforged.server.core.managers.UtilityManager;
import com.darwinreforged.server.core.modules.Module;
import com.darwinreforged.server.core.resources.Permissions;
import com.darwinreforged.server.core.resources.translations.Translation;
import com.darwinreforged.server.core.types.internal.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

@SuppressWarnings({"InstantiationOfUtilityClass", "unchecked"})
public abstract class DarwinServer extends Singleton {

    protected static final String MODULE_PACKAGE = "com.darwinreforged.server.modules";
    protected static final String CORE_PACKAGE = "com.darwinreforged.server.core";

    public static final String AUTHOR = "GuusLieben";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private EventBus eventBus;
    private LibraryManager libMan;
    private ModuleManager modMan;
    private UtilityManager utilMan;
    private String version;
    private String lastUpdate;
    private DarwinConfig config;

    public DarwinServer() throws InstantiationException {
    }

    public static Logger getLog() {
        return getServer().log;
    }

    public static String getVersion() {
        return (instance == null || getServer().version == null) ? "Unknown-dev" : getServer().version;
    }

    public static String getLastUpdate() {
        return (instance == null || getServer().lastUpdate == null) ? "Unknown" : getServer().lastUpdate;
    }

    public static String getAuthor() {
        return AUTHOR;
    }

    public static void error(String message) {
        error(message, new Exception());
    }

    public static void error(String message, Throwable e) {
        if (DarwinConfig.FRIENDLY_ERRORS.get()) {

            String err = String.join("", Collections.nCopies(5, "=")) +
                    e.getClass().toGenericString().split("\\.")[2] +
                    String.join("", Collections.nCopies(5, "=")) +
                    "\n" +
                    String.format(" Message -> %s%n", e.getMessage()) +
                    String.format(" Source -> %s%n", e.getStackTrace()[0].getClassName()) +
                    String.format(" Method -> %s%n", e.getStackTrace()[0].getMethodName()) +
                    String.format(" Line -> %d%n", e.getStackTrace()[0].getLineNumber()) +
                    String.format(" Additional message -> %s%n", message);
            getLog().error(err);
        }
        if (DarwinConfig.STACKTRACES.get()) {
            e.printStackTrace();
        }
        if (!DarwinConfig.STACKTRACES.get() && !DarwinConfig.FRIENDLY_ERRORS.get()) {
            getLog().error(e.getMessage());
        }
    }

    public static DarwinServer getServer() {
        return (DarwinServer) DarwinServer.instance;
    }

    private static boolean verifyAlive() {
        Map<String, Object> stor = getServer().utilMan.get(FileManager.class).getYamlDataForUrl("http://dockbox.org/darwin/stor/darwin.yml");
        if (stor.containsKey("keepalive")) return Boolean.parseBoolean(stor.get("keepalive").toString());
        return false;
    }

    public static LibraryManager getLibMan() {
        return getServer().libMan;
    }

    public static ModuleManager getModMan() {
        return getServer().modMan;
    }

    public static UtilityManager getUtilMan() {
        return getServer().utilMan;
    }

    public static EventBus getEventBus() {
        return getServer().eventBus;
    }

    protected void setup() throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/darwin.properties"));
        version = properties.getOrDefault("version", "Unknown-dev").toString();
        lastUpdate = properties.getOrDefault("last_update", "Unknown").toString();

        this.libMan = new LibraryManager();
        this.libMan.checkLibraries();
        getLog().info("Got libraries");

        this.utilMan = new UtilityManager(DarwinServer.CORE_PACKAGE);
        this.utilMan.scanUtilities(getServer().getClass());

        if (!verifyAlive()) {
            throw new IllegalStateException("DarwinServer will not be loaded");

        } else {
            this.eventBus = new EventBus();

            log.info("Loading integrated modules");
            this.modMan.scanModulePackage(MODULE_PACKAGE, true, this.modMan::registerClasses);

            config = new DarwinConfig();

            DiscordChatManager du = this.utilMan.get(DiscordChatManager.class);
            du.init(DarwinConfig.DISCORD_CHANNEL_WHITELIST.get());

            if (DarwinConfig.LOAD_EXTERNAL_MODULES.get()) {
                log.info("Loading external modules");
                this.modMan.loadExternalModules();
            }

            Permissions.collect();
            Translation.initTranslationService();

            CommandBus<?, ?> cb = DarwinServer.get(CommandBus.class);
            cb.register(instance.getClass());
            cb.register(DarwinServer.class); // For dserver command

            this.eventBus.subscribe(this);
        }
    }

    public static <I> I get(Class<I> clazz) {
        Supplier<IllegalStateException> exc = () -> new IllegalStateException(String.format("Could not obtain instance of %s", clazz.toGenericString()));
        if (clazz.isAnnotationPresent(Utility.class)) {
            Optional<? extends I> optionalImpl = getServer().utilMan.getUtil(clazz);
            return optionalImpl.orElseThrow(exc);
        } else if (clazz.isAnnotationPresent(Module.class)) {
            Optional<I> optionalMod = getModMan().getModule(clazz);
            return optionalMod.orElseThrow(exc);
        }
        throw exc.get();
    }

    public static void reload() {
        getServer().config = new DarwinConfig();
        Permissions.collect();
        Translation.initTranslationService();
        DarwinServer.getLog().info("Successfully reloaded DarwinServer configurations");
    }

    public abstract File getLibraryDirectory();

    public abstract void stopServer(String message);

    public abstract ServerType getServerType();

    public abstract void runAsync(Runnable runnable);

    public abstract void runOnMainThread(Runnable runnable);

}
