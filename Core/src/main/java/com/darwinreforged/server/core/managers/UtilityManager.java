package com.darwinreforged.server.core.managers;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.internal.Utility;

import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class UtilityManager {

    private final Map<Class<?>, Object> UTILS = new ConcurrentHashMap<>();
    private final String corePackage;

    public UtilityManager(String corePackage) {
        this.corePackage = corePackage;
    }

    public void injectUtil(Class<?> type, Object util) {
        UTILS.put(type, util);
    }

    public <I> Optional<? extends I> getUtil(Class<I> clazz) {
        if (!clazz.isAnnotationPresent(Utility.class))
            throw new IllegalArgumentException(String.format("Requested utility class is not annotated as such (%s)", clazz.toGenericString()));
        Object implementation = UTILS.get(clazz);
        if (implementation != null) return (Optional<? extends I>) Optional.of(implementation);
        return Optional.empty();
    }

    public <I> I get(Class<I> clazz) {
        return getUtil(clazz).orElseThrow(() -> new IllegalStateException("Requested utility does not exist, or is not implemented"));
    }

    public void scanUtilities(Class<? extends DarwinServer> implementation) {
        Reflections abstrPackRef = new Reflections(this.corePackage);
        Reflections implPackRef = new Reflections(implementation.getPackage().getName());
        Set<Class<?>> abstractUtils = abstrPackRef.getTypesAnnotatedWith(Utility.class);

        abstractUtils.forEach(abstractUtil -> {
            Set<Class<?>> candidates = implPackRef.getSubTypesOf((Class<Object>) abstractUtil);
            if (candidates.size() == 1) {
                try {
                    Class<?> impl = new ArrayList<>(candidates).get(0);
                    injectUtil(abstractUtil, impl.newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    DarwinServer.error("Failed to create instance of utility type", e);
                }
            } else {
                throw new RuntimeException("Missing implementation for : " + abstractUtil.getSimpleName());
            }
        });
    }

}
