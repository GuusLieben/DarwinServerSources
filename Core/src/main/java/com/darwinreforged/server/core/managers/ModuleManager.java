package com.darwinreforged.server.core.managers;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.commands.CommandBus;
import com.darwinreforged.server.core.files.FileManager;
import com.darwinreforged.server.core.internal.ModuleRegistration;
import com.darwinreforged.server.core.modules.DisabledModule;
import com.darwinreforged.server.core.modules.Module;
import com.darwinreforged.server.core.resources.Dependencies;
import com.darwinreforged.server.core.tuple.Tuple;

import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ModuleManager {

    private final Map<Class<?>, Tuple<Object, Module>> modules = new ConcurrentHashMap<>();
    private final Map<String, String> moduleSources = new ConcurrentHashMap<>();
    private final List<String> failedModules = new CopyOnWriteArrayList<>();

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

            DarwinServer.getEventBus().subscribe(instance);
            CommandBus<?, ?> cb = DarwinServer.getUtilMan().get(CommandBus.class);
            cb.register(instance.getClass());
            // Do not register the same module twice
            if (getModule(module).isPresent()) return ModuleRegistration.SUCCEEDED;
            this.modules.put(module, new Tuple<>(instance, moduleInfo));
            this.moduleSources.put(moduleInfo.id(), source);

            if (deprecatedModule != null) return ModuleRegistration.DEPRECATED_AND_SUCCEEDED;
            else return ModuleRegistration.SUCCEEDED;

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            this.failedModules.add(module.getSimpleName());
            if (deprecatedModule != null)
                return ModuleRegistration.DEPRECATED_AND_FAIL.setCtx(e.getMessage());
            else return ModuleRegistration.FAILED.setCtx(e.getMessage());
        }
    }

    public void loadExternalModules() {
        Path modDir = DarwinServer.getUtilMan().get(FileManager.class).getModuleDirectory();
        try {
            URL url = modDir.toUri().toURL();
            DarwinServer.getLog().info(String.format("Scanning %s for additional modules", url.toString()));
            Arrays.stream(Objects.requireNonNull(modDir.toFile().listFiles()))
                    .filter(f -> f.getName().endsWith(".jar"))
                    .forEach(this::scanModulesInFile);
        } catch (MalformedURLException e) {
            DarwinServer.error("Failed to load additional modules", e);
        }
    }

    private void scanModulesInFile(File moduleCandidate) {
        try {
            DarwinServer.getLibMan().injectJarToClassPath(moduleCandidate, clazz -> {
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
                        DarwinServer.error(String.format("Found module candidate without defined package '%s' at %s%n", clazz.toGenericString(), moduleCandidate.getName()));
                        return;
                    }

                    registerClasses(moduleCandidate.getName(), clazz);
                }
            });
        } catch (IOException | ClassNotFoundException e) {
            DarwinServer.error(String.format("Failed to register potential module file '%s'", moduleCandidate.getName()), e);
        }
    }

    public void registerClasses(String source, Class<?>... pluginModules) {
        AtomicInteger done = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        Arrays.stream(pluginModules).forEach(mod -> {
            ModuleRegistration result = registerModule(mod, source);
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

    public void scanModulePackage(String pkg, boolean integrated, BiConsumer<String, Class<?>[]> consumer) {
        scanModulePackage(pkg, integrated ? "Integrated" : "Unknown", consumer);
    }

    public void scanModulePackage(String packageString, String source, BiConsumer<String, Class<?>[]> consumer) {
        if ("".equals(packageString)) return;
        Reflections reflections = new Reflections(packageString);
        Set<Class<?>> pluginModules = reflections
                .getTypesAnnotatedWith(Module.class);
        if (pluginModules.isEmpty()) return;

        consumer.accept(source, pluginModules.toArray(new Class[0]));
    }

    public <I> Optional<I> getModule(Class<I> clazz) {
        return getModDataTuple(clazz).map(Tuple::getFirst);
    }

    public <I> Optional<I> getModule(String id) {
        return (Optional<I>) getModDataTuple(id).map(Tuple::getFirst);
    }

    public Optional<Module> getModuleInfo(Class<?> clazz) {
        return getModDataTuple(clazz).map(Tuple::getSecond);
    }

    public Optional<Module> getModuleInfo(String id) {
        return getModDataTuple(id).map(Tuple::getSecond);
    }

    public String getModuleSource(String id) {
        return this.moduleSources.get(id);
    }

    public List<Module> getAllModuleInfo() {
        return modules.values().stream().map(Tuple::getSecond).collect(Collectors.toList());
    }

    private <I> Optional<Tuple<I, Module>> getModDataTuple(String id) {
        try {
            return modules.values().stream().filter(objectModuleTuple -> (objectModuleTuple.getSecond().id().equals(id)))
                    .map(objectModuleTuple -> (Tuple<I, Module>) objectModuleTuple).findFirst();
        } catch (ClassCastException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private <I> Optional<Tuple<I, Module>> getModDataTuple(Class<I> clazz) {
        try {
            Tuple<Object, Module> module = modules
                    .getOrDefault(clazz, null);
            return Optional.ofNullable((Tuple<I, Module>) module);
        } catch (ClassCastException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Map<Class<?>, Tuple<Object, Module>> getModules() {
        return modules;
    }

    public Map<String, String> getModuleSources() {
        return moduleSources;
    }

    public List<String> getFailedModules() {
        return failedModules;
    }
}
