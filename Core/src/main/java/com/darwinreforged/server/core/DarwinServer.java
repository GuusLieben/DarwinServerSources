package com.darwinreforged.server.core;

import com.darwinreforged.server.core.chat.ClickEvent;
import com.darwinreforged.server.core.chat.ClickEvent.ClickAction;
import com.darwinreforged.server.core.chat.DiscordChatManager;
import com.darwinreforged.server.core.chat.HoverEvent;
import com.darwinreforged.server.core.chat.HoverEvent.HoverAction;
import com.darwinreforged.server.core.chat.Pagination;
import com.darwinreforged.server.core.chat.Pagination.PaginationBuilder;
import com.darwinreforged.server.core.chat.Text;
import com.darwinreforged.server.core.commands.CommandBus;
import com.darwinreforged.server.core.commands.annotations.Command;
import com.darwinreforged.server.core.commands.context.CommandArgument;
import com.darwinreforged.server.core.commands.context.CommandContext;
import com.darwinreforged.server.core.events.internal.server.ServerReloadEvent;
import com.darwinreforged.server.core.events.util.EventBus;
import com.darwinreforged.server.core.events.util.Listener;
import com.darwinreforged.server.core.files.FileManager;
import com.darwinreforged.server.core.internal.DarwinConfig;
import com.darwinreforged.server.core.internal.ServerType;
import com.darwinreforged.server.core.internal.Utility;
import com.darwinreforged.server.core.modules.DisabledModule;
import com.darwinreforged.server.core.modules.Module;
import com.darwinreforged.server.core.resources.Dependencies;
import com.darwinreforged.server.core.resources.Permissions;
import com.darwinreforged.server.core.resources.ServerDependencies;
import com.darwinreforged.server.core.resources.translations.DefaultTranslations;
import com.darwinreforged.server.core.resources.translations.Translation;
import com.darwinreforged.server.core.tuple.Tuple;
import com.darwinreforged.server.core.types.internal.Singleton;
import com.darwinreforged.server.core.types.living.CommandSender;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

@SuppressWarnings({"InstantiationOfUtilityClass", "unchecked"})
public abstract class DarwinServer extends Singleton {

    protected static final Map<Class<?>, Tuple<Object, Module>> MODULES = new ConcurrentHashMap<>();
    protected static final Map<String, String> MODULE_SOURCES = new ConcurrentHashMap<>();
    protected static final List<String> FAILED_MODULES = new CopyOnWriteArrayList<>();
    protected static final Map<Class<?>, Object> UTILS = new ConcurrentHashMap<>();
    protected static final String MODULE_PACKAGE = "com.darwinreforged.server.modules";
    protected static final String CORE_PACKAGE = "com.darwinreforged.server.core";
    protected static final String RESOURCE_PACKAGE = "com.darwinreforged.server.core.resources";

    public static final String AUTHOR = "GuusLieben";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private EventBus eventBus;
    private String version;
    private String lastUpdate;
    private DarwinConfig config;


    public DarwinServer() throws InstantiationException {
    }

    public static Logger getLog() {
        return getServer().log;
    }

    protected void setupPlatform() throws IOException {
        // Load plugin properties
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/darwin.properties"));
        version = properties.getOrDefault("version", "Unknown-dev").toString();
        lastUpdate = properties.getOrDefault("last_update", "Unknown").toString();

        // Make sure all libraries are present
        checkLibraries();

        // Create utility implementations
        scanUtilities(getServer().getClass());

        if (!verifyAlive()) {
            throw new IllegalStateException("DarwinServer will not be loaded");

        } else {
            // Create event bus
            this.eventBus = new EventBus();

            // Create integrated modules (in server jar)
            log.info("Loading integrated modules");
            scanModulePackage(MODULE_PACKAGE, true);

            // Set up config
            config = new DarwinConfig();

            // Registering JDA Listeners
            DiscordChatManager du = get(DiscordChatManager.class);
            du.init(DarwinConfig.DISCORD_CHANNEL_WHITELIST.get());

            if (DarwinConfig.LOAD_EXTERNAL_MODULES.get()) {
                // Create external modules (outside server jar, inside modules folder)
                log.info("Loading external modules");
                loadExternalModules();
            }

            // Import permissions and translations
            Permissions.collect();
            Translation.initTranslationService();

            // Setting up commands
            CommandBus<?, ?> cb = get(CommandBus.class);
            cb.register(instance.getClass());
            cb.register(DarwinServer.class); // For dserver command

            this.eventBus.subscribe(this);
        }
    }
    public DarwinConfig getConfig() {
        return config;
    }

    public static String getVersion() {
        return (instance == null || getServer().version == null) ? "Unknown-dev" : getServer().version;
    }

    public static String getLastUpdate() {
        return (instance == null || getServer().lastUpdate == null) ? "Unknown" : getServer().lastUpdate;
    }

    private void loadExternalModules() {
        Path modDir = get(FileManager.class).getModuleDirectory();
        try {
            URL url = modDir.toUri().toURL();
            log.info(String.format("Scanning %s for additional modules", url.toString()));
            Arrays.stream(Objects.requireNonNull(modDir.toFile().listFiles()))
                    .filter(f -> f.getName().endsWith(".jar"))
                    .forEach(this::scanModulesInFile);
        } catch (MalformedURLException e) {
            error("Failed to load additional modules", e);
        }
    }

    public static void error(String message) {
        error(message, new Exception());
    }

    public static void error(String message, Throwable e) {
        if (DarwinConfig.FRIENDLY_ERRORS.get()) {
            StringBuilder b = new StringBuilder();

            String err = b.append(String.join("", Collections.nCopies(5, "=")))
                    .append(e.getClass().toGenericString().split("\\.")[2])
                    .append(String.join("", Collections.nCopies(5, "=")))
                    .append("\n")
                    .append(String.format(" Message -> %s%n", e.getMessage()))
                    .append(String.format(" Source -> %s%n", e.getStackTrace()[0].getClassName()))
                    .append(String.format(" Method -> %s%n", e.getStackTrace()[0].getMethodName()))
                    .append(String.format(" Line -> %d%n", e.getStackTrace()[0].getLineNumber()))
                    .append(String.format(" Additional message -> %s%n", message))
                    .toString();
            getServer().log.error(err);
        }
        if (DarwinConfig.STACKTRACES.get()) {
            e.printStackTrace();
        }
        if (!DarwinConfig.STACKTRACES.get() && !DarwinConfig.FRIENDLY_ERRORS.get()) {
            getServer().log.error(e.getMessage());
        }
    }

    private void scanModulesInFile(File moduleCandidate) {
        try {
            injectJarToClassPath(moduleCandidate, clazz -> {
                // As classes are external it doesn't match Class types, generic string values are however the same
                if (clazz.isAnnotationPresent(Module.class)) {
                    // Make sure there is a constructor applicable before accepting it
                    try {
                        clazz.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    // Require modules to have a dedicated package
                    if (clazz.getPackage() == null) {
                        error(String.format("Found module candidate without defined package '%s' at %s%n", clazz.toGenericString(), moduleCandidate.getName()));
                        return;
                    }

                    registerClasses(moduleCandidate.getName(), clazz);
                }
            });
        } catch (IOException | ClassNotFoundException e) {
            error(String.format("Failed to register potential module file '%s'", moduleCandidate.getName()), e);
        }
    }

    private void scanUtilities(Class<? extends DarwinServer> implementation) {
        Reflections abstrPackRef = new Reflections(CORE_PACKAGE);
        Reflections implPackRef = new Reflections(implementation.getPackage().getName());
        Set<Class<?>> abstractUtils = abstrPackRef.getTypesAnnotatedWith(Utility.class);

        abstractUtils.forEach(abstractUtil -> {
            Set<Class<?>> candidates = implPackRef.getSubTypesOf((Class<Object>) abstractUtil);
            if (candidates.size() == 1) {
                try {
                    Class<?> impl = new ArrayList<>(candidates).get(0);
                    UTILS.put(abstractUtil, impl.newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    DarwinServer.error("Failed to create instance of utility type", e);
                }
            } else {
                throw new RuntimeException("Missing implementation for : " + abstractUtil.getSimpleName());
            }
        });
    }

    public abstract ServerType getServerType();

    public static EventBus getEventBus() {
        return getServer().eventBus;
    }

    private static <I> Optional<? extends I> getUtil(Class<I> clazz) {
        if (!clazz.isAnnotationPresent(Utility.class))
            throw new IllegalArgumentException(String.format("Requested utility class is not annotated as such (%s)", clazz.toGenericString()));
        Object implementation = UTILS.get(clazz);
        if (implementation != null) return (Optional<? extends I>) Optional.of(implementation);
        return Optional.empty();
    }

    public static <I> I get(Class<I> clazz) {
        Optional<? extends I> optionalImpl = getUtil(clazz);
        return optionalImpl.orElseThrow(() -> new IllegalStateException(String.format("Could not obtain instance of %s", clazz.toGenericString())));
    }

    public static <I> Optional<I> getModule(Class<I> clazz) {
        return getModDataTuple(clazz).map(Tuple::getFirst);
    }


    public static <I> Optional<I> getModule(String id) {
        return (Optional<I>) getModDataTuple(id).map(Tuple::getFirst);
    }


    private static <I> Optional<Tuple<I, Module>> getModDataTuple(String id) {
        try {
            return MODULES.values().stream().filter(objectModuleTuple -> (objectModuleTuple.getSecond().id().equals(id)))
            .map(objectModuleTuple -> (Tuple<I, Module>) objectModuleTuple).findFirst();
        } catch (ClassCastException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }


    private static <I> Optional<Tuple<I, Module>> getModDataTuple(Class<I> clazz) {
        try {
            Tuple<Object, Module> module = MODULES
                    .getOrDefault(clazz, null);
            return Optional.ofNullable((Tuple<I, Module>) module);
        } catch (ClassCastException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<Module> getModuleInfo(Class<?> clazz) {
        return getModDataTuple(clazz).map(Tuple::getSecond);
    }

    public static Optional<Module> getModuleInfo(String id) {
        return getModDataTuple(id).map(Tuple::getSecond);
    }

    public static String getModuleSource(String id) {
        return DarwinServer.MODULE_SOURCES.get(id);
    }

    protected ModuleRegistration registerModule(Class<?> module, String source) {
        Deprecated deprecatedModule = module.getAnnotation(Deprecated.class);

        // Disabled module
        DisabledModule disabledModule = module.getAnnotation(DisabledModule.class);
        if (disabledModule != null) {
            return ModuleRegistration.DISABLED.setCtx(disabledModule.value());
        }

        try {
            Constructor<?> constructor = module.getDeclaredConstructor();
            Object instance = constructor.newInstance();

            Module moduleInfo = module.getAnnotation(Module.class);
            if (moduleInfo == null) throw new InstantiationException("No module info was provided");
            for (Dependencies dependency : moduleInfo.dependencies()) {
                if (!dependency.isLoaded())
                    return ModuleRegistration.DISABLED.setCtx(String.format("Required dependency '%s' is not present.", dependency.getMainClass()));
            }

            registerListener(instance);
            CommandBus<?, ?> cb = get(CommandBus.class);
            cb.register(instance.getClass());
            // Do not register the same module twice
            if (getModule(module).isPresent()) return ModuleRegistration.SUCCEEDED;
            DarwinServer.MODULES.put(module, new Tuple<>(instance, moduleInfo));
            DarwinServer.MODULE_SOURCES.put(moduleInfo.id(), source);

            if (deprecatedModule != null) return ModuleRegistration.DEPRECATED_AND_SUCCEEDED;
            else return ModuleRegistration.SUCCEEDED;

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            DarwinServer.FAILED_MODULES.add(module.getSimpleName());
            if (deprecatedModule != null)
                return ModuleRegistration.DEPRECATED_AND_FAIL.setCtx(e.getMessage());
            else return ModuleRegistration.FAILED.setCtx(e.getMessage());
        }
    }

    public void registerListener(Object obj) {
        getEventBus().subscribe(obj);
    }

    public static DarwinServer getServer() {
        return (DarwinServer) DarwinServer.instance;
    }

    public static List<Module> getAllModuleInfo() {
        return MODULES.values().stream().map(Tuple::getSecond).collect(Collectors.toList());
    }

    public void scanModulePackage(String pkg, boolean integrated) {
        scanModulePackage(pkg, integrated ? "Integrated" : "Unknown");
    }

    private void registerClasses(String source, Class<?>... pluginModules) {
        AtomicInteger done = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        Arrays.stream(pluginModules).forEach(mod -> {
            DarwinServer.ModuleRegistration result = registerModule(mod, source);
            switch (result) {
                case DEPRECATED_AND_FAIL:
                case FAILED:
                    failed.getAndIncrement();
                    break;
                case SUCCEEDED:
                case DEPRECATED_AND_SUCCEEDED:
                    done.getAndIncrement();
                    break;
                case DISABLED:
                    break;
            }
        });
    }

    public void scanModulePackage(String packageString, String source) {
        if ("".equals(packageString)) return;
        Reflections reflections = new Reflections(packageString);
        Set<Class<?>> pluginModules = reflections
                .getTypesAnnotatedWith(Module.class);
        if (pluginModules.isEmpty()) return;

        registerClasses(source, pluginModules.toArray(new Class[0]));
    }

    @Command(aliases = "dserver", usage = "dserver [module]", desc = "Returns active and failed modules to the player", min = 0, context = "dserver [module{Module}]")
    public void commandList(CommandSender src, CommandContext ctx) {
        Optional<CommandArgument<Module>> moduleCandidate = ctx.getArgument("module", Module.class);

        if (moduleCandidate.isPresent()) {
            Module mod = moduleCandidate.get().getValue();
            String[] dependencies = Arrays.stream(mod.dependencies()).map(dep ->
                    DefaultTranslations.DARWIN_SINGLE_MODULE_DEPENDENCY.f(dep.toString().toLowerCase(), dep.getMainClass(), dep.isLoaded() ? "Present" : "Absent")
            ).toArray(String[]::new);
            String source = DefaultTranslations.MODULE_SOURCE.f(MODULE_SOURCES.get(mod.id()));

            Text message = Text.of(
                    DefaultTranslations.DARWIN_SINGLE_MODULE_HEADER.f(mod.name()), '\n',
                    DefaultTranslations.DARWIN_SINGLE_MODULE_DATA.f(
                            mod.id(),
                            mod.name(),
                            mod.description(),
                            mod.version(),
                            mod.url(),
                            dependencies.length > 0 ? String.join(", ", dependencies) : DefaultTranslations.NONE.s().toLowerCase(),
                            mod.authors().length > 0 ? String.join(", ", mod.authors()) : DefaultTranslations.UNKNOWN.s().toLowerCase(),
                            source
                    )
            );
            src.sendMessage(message, false);

        } else {
            List<Text> moduleContext = new ArrayList<>();
            MODULES.forEach((clazz, ignored) -> {
                Optional<Module> infoOptional = getModuleInfo(clazz);
                if (infoOptional.isPresent()) {
                    Module info = infoOptional.get();
                    String name = info.name();
                    String id = info.id();
                    boolean disabled = clazz.getAnnotation(DisabledModule.class) != null;
                    String source = DefaultTranslations.MODULE_SOURCE.f(MODULE_SOURCES.get(id));
                    Text activeModule = Text.of(DefaultTranslations.ACTIVE_MODULE_ROW.f(name, id, source));
                    activeModule.setHoverEvent(new HoverEvent(HoverAction.SHOW_TEXT, DefaultTranslations.DARWIN_SERVER_MODULE_HOVER.f(id)));
                    activeModule.setClickEvent(new ClickEvent(ClickAction.RUN_COMMAND, "/dserver " +id));
                    moduleContext.add(disabled ? Text.of(DefaultTranslations.DISABLED_MODULE_ROW.f(name, id, source))
                            : activeModule);
                }
            });
            FAILED_MODULES.forEach(module -> moduleContext.add(Text.of(DefaultTranslations.FAILED_MODULE_ROW.f(module))));

            Text header = Text.of(DefaultTranslations.DARWIN_SERVER_VERSION.f(getVersion()))
                    .append(Text.NEW_LINE)
                    .append(DefaultTranslations.DARWIN_SERVER_UPDATE.f(getLastUpdate()))
                    .append(Text.NEW_LINE)
                    .append(DefaultTranslations.DARWIN_SERVER_AUTHOR.f(AUTHOR))
                    .append(Text.NEW_LINE)
                    .append(DefaultTranslations.DARWIN_SERVER_MODULE_HEAD.s());

            PaginationBuilder builder = Pagination.builder();
            builder
                    .title(Text.of(DefaultTranslations.DARWIN_MODULE_TITLE.s()))
                    .padding(Text.of(DefaultTranslations.DARWIN_MODULE_PADDING.s()))
                    .contents(moduleContext)
                    .header(header)
                    .build().sendTo(src);
        }
    }

    @Listener
    public void onServerReload(ServerReloadEvent event) {
        this.config = new DarwinConfig();
        Permissions.collect();
        Translation.initTranslationService();
        getLog().info("Successfully reloaded DarwinServer configurations");
    }

    private void checkLibraries() {
        Arrays.stream(ServerDependencies.values()).forEach(serverDependency ->
                checkOrDownloadLibrary(serverDependency.getBasePackage(), serverDependency.getUrl(), serverDependency.getVersion(), serverDependency.getBaseFile()));
    }

    protected void checkOrDownloadLibrary(String pkg, String url, String version, String fileName) {
        if (Package.getPackage(pkg) == null) {
            try {
                File file = new File(getLibraryDirectory(), fileName);
                if (!file.exists()) {
                    URL remoteUrl = new URL(url);
                    InputStream inputStream = remoteUrl.openStream();
                    Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                injectJarToClassPath(file);
            } catch (IOException | ClassNotFoundException e) {
                error(String.format("Failed to download library '%s' version %s from '%s'", fileName, version, url), e);
                stopServer(String.format("Failed to download library '%s' version %s from '%s'", fileName, version, url));
            }
        }
    }

    private void injectJarToClassPath(File file) throws IOException, ClassNotFoundException {
        injectJarToClassPath(file, null);
    }

    private void injectJarToClassPath(File file, Consumer<Class<?>> consumer) throws IOException, ClassNotFoundException {
        if (file != null && file.exists() && file.getName().endsWith(".jar")) {
            URLClassLoader ucl = URLClassLoader.newInstance(
                    new URL[]{file.toURI().toURL()},
                    this.getClass().getClassLoader());
            JarFile jarFile = new JarFile(file);

            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    Class<?> clazz;
                    String className = entry.getName().replace("/", ".").replace(".class", "");
                    // Inject into classpath
                    try {
                        clazz = ucl.loadClass(className);
                    } catch (ClassNotFoundException e) {
                        clazz = Class.forName(className, true, ucl);
                    }

                    if (consumer != null) consumer.accept(clazz);
                }
            }
        }
    }

    protected abstract File getLibraryDirectory();

    protected abstract void stopServer(String message);

    public abstract void runAsync(Runnable runnable);

    public abstract void runOnMainThread(Runnable runnable);

    private static boolean verifyAlive() {
        Map<String, Object> stor = get(FileManager.class).getYamlDataForUrl("http://dockbox.org/darwin/stor/darwin.yml");
        if (stor.containsKey("keepalive")) return Boolean.parseBoolean(stor.get("keepalive").toString());
        return false;
    }

    public enum ModuleRegistration {
        DISABLED,
        DEPRECATED_AND_FAIL,
        DEPRECATED_AND_SUCCEEDED,
        FAILED,
        SUCCEEDED;

        String ctx;

        ModuleRegistration() {
        }

        public ModuleRegistration setCtx(String ctx) {
            this.ctx = ctx;
            return this;
        }

        public String getContext() {
            return this.ctx;
        }

    }

}
