package com.darwinreforged.server.core.internal;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.modules.Module;

import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.BiConsumer;

public class StartupHelper {


    public static void scanUtilities(Class<? extends DarwinServer> implementation) {
        Reflections abstrPackRef = new Reflections(DarwinServer.getCorePackage());
        Reflections implPackRef = new Reflections(implementation.getPackage().getName());
        Set<Class<?>> abstractUtils = abstrPackRef.getTypesAnnotatedWith(Utility.class);

        abstractUtils.forEach(abstractUtil -> {
            Set<Class<?>> candidates = implPackRef.getSubTypesOf((Class<Object>) abstractUtil);
            if (candidates.size() == 1) {
                try {
                    Class<?> impl = new ArrayList<>(candidates).get(0);
                    DarwinServer.injectUtil(abstractUtil, impl.newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    DarwinServer.error("Failed to create instance of utility type", e);
                }
            } else {
                throw new RuntimeException("Missing implementation for : " + abstractUtil.getSimpleName());
            }
        });
    }


    public static void scanModulePackage(String pkg, boolean integrated, BiConsumer<String, Class<?>[]> consumer) {
        scanModulePackage(pkg, integrated ? "Integrated" : "Unknown", consumer);
    }

    public static void scanModulePackage(String packageString, String source, BiConsumer<String, Class<?>[]> consumer) {
        if ("".equals(packageString)) return;
        Reflections reflections = new Reflections(packageString);
        Set<Class<?>> pluginModules = reflections
                .getTypesAnnotatedWith(Module.class);
        if (pluginModules.isEmpty()) return;

        consumer.accept(source, pluginModules.toArray(new Class[0]));
    }
}
