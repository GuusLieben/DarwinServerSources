/*
 * Copyright 2019-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dockbox.hartshorn.spi;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.dockbox.hartshorn.util.CollectionUtilities;
import org.dockbox.hartshorn.util.collections.ConcurrentSetMultiMap;
import org.dockbox.hartshorn.util.collections.MultiMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A service that allows for the discovery of implementations of a given type. This service is a utility wrapper around
 * {@link ServiceLoader}, and allows for the discovery of implementations of a given type in a more convenient way.
 *
 * <p>This service allows for the discovery of implementations through SPI, as well as manual overrides through
 * {@link #override(Class, Class)} (or {@link #override(Class, String)} if the implementation is not available at compile
 * time. This service also allows for the addition of additional class loaders, which allows for the discovery of
 * implementations that are not available through the class loader of the service itself.
 *
 * <p>Implementations are expected to have a default constructor, and be assignable from the type that is being discovered.
 *
 * <p>Implementations are resolved in the following order:
 * <ol>
 *     <li>Cached implementations from prior discovery</li>
 *     <li>Manual override with a qualified name or loaded {@link Class}</li>
 *     <li>Service discovery through SPI</li>
 * </ol>
 *
 * <p>Implementations are cached, and will only be released if an override is modified.
 *
 * @see ServiceLoader
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public final class DiscoveryService {

    private static DiscoveryService DISCOVERY_SERVICE = new DiscoveryService();

    private final MultiMap<Class<?>, Class<?>> types = new ConcurrentSetMultiMap<>();
    private final MultiMap<String, String> overrideDiscoveryFiles = new ConcurrentSetMultiMap<>();
    private final Set<ClassLoader> classLoaders = new HashSet<>(List.of( // List, as context class loader may be same as service's class loader
            Thread.currentThread().getContextClassLoader(),
            DiscoveryService.class.getClassLoader()
    ));

    private DiscoveryService() {
        if (DISCOVERY_SERVICE != null) {
            throw new IllegalStateException("Already instantiated");
        }
        // Check that the class is being created by itself
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < 3 || !stackTrace[2].getClassName().equals(DiscoveryService.class.getName())) {
            throw new IllegalStateException("Cannot instantiate outside of class");
        }
    }

    /**
     * Returns the singleton instance of the service.
     *
     * @return the singleton instance of the service
     */
    @NonNull
    public static synchronized DiscoveryService instance() {
        if (DISCOVERY_SERVICE == null) {
            DISCOVERY_SERVICE = new DiscoveryService();
        }
        return DISCOVERY_SERVICE;
    }

    /**
     * Indicates whether the current service has an implementation for the given type. The implementation may
     * have been cached from a prior discovery, or may be available through overrides or SPI. This method does not
     * indicate whether the implementation is sufficiently available to be loaded.
     *
     * @param type the type to check for
     *
     * @return {@code true} if an implementation is available, {@code false} otherwise
     */
    public boolean contains(Class<?> type) {
        if (this.types.containsKey(type) || this.overrideDiscoveryFiles.containsKey(type.getName())) {
            return true;
        }
        for (ClassLoader classLoader : this.classLoaders) {
            ServiceLoader<?> serviceLoader = ServiceLoader.load(type, classLoader);
            // Stream, to get the Provider instead of an instance of the implementation.
            if (serviceLoader.stream().findFirst().isPresent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Discovers an implementation of the given type. If an implementation is available through overrides or SPI, it
     * will be returned. If no implementation is available, a {@link ServiceDiscoveryException} will be thrown. As such,
     * this method is not suitable for optional dependencies, and should only be used for required dependencies. If
     * optional dependencies are required, use {@link #contains(Class)} to check for availability.
     *
     * @param type the type to discover an implementation for
     * @param <T> the type to discover an implementation for
     *
     * @return an implementation of the given type
     *
     * @throws ServiceDiscoveryException if no implementation is available, or an error occurs during discovery
     */
    @NonNull
    public <T> T discover(Class<T> type) throws ServiceDiscoveryException {
        try {
            Set<T> objects = this.tryLoadDiscoveryFile(type);
            if (objects.size() > 1) {
                throw new ServiceDiscoveryException("Multiple implementations found for type " + type.getCanonicalName());
            }
            T object = CollectionUtilities.first(objects);
            if (object != null) {
                return object;
            }
        }
        catch(NoAvailableImplementationException e) {
            throw new ServiceDiscoveryException(e.getMessage(), e);
        }
        throw new ServiceDiscoveryException("No implementation found for type " + type.getCanonicalName());
    }

    public <T> Set<T> discoverAll(Class<T> type) throws ServiceDiscoveryException {
        try {
            return this.tryLoadDiscoveryFile(type);
        }
        catch(NoAvailableImplementationException e) {
            throw new ServiceDiscoveryException(e.getMessage(), e);
        }
    }

    /**
     * Overrides the discovery of an implementation for the given type. The implementation must be assignable from the
     * given type, and must have a default constructor. If the implementation is not available at compile time, use
     * {@link #override(Class, String)} instead.
     *
     * @param type the type to override
     * @param implementation the implementation to use
     */
    public void override(Class<?> type, Class<?> implementation) {
        if (type.isAssignableFrom(implementation)) {
            this.override(type, implementation.getName());
        }
        else {
            throw new IllegalArgumentException("Implementation " + implementation.getName() + " is not assignable from type " + type.getName());
        }
    }

    /**
     * Overrides the discovery of an implementation for the given type. The implementation must be assignable from the
     * given type, and must have a default constructor. As the implementation is not available at compile time, these
     * rules are validated at discovery time.
     *
     * @param type the type to override
     * @param qualifiedName the qualified name of the implementation to use
     */
    public void override(Class<?> type, String qualifiedName) {
        this.overrideDiscoveryFiles.put(type.getName(), qualifiedName);
        // Release cached implementation, if any exists
        this.types.remove(type);
    }

    /**
     * Adds a class loader to the service. This allows for the discovery of implementations that are not available
     * through the class loader of the service itself.
     *
     * @param classLoader the class loader to add
     */
    public void addClassLoader(ClassLoader classLoader) {
        this.classLoaders.add(classLoader);
    }

    private <T> Set<T> tryLoadDiscoveryFile(Class<T> type) throws ServiceDiscoveryException {
        Collection<Class<?>> implementationClasses = this.tryLoadImplementationClass(type);
        if (!implementationClasses.isEmpty()) {
            Set<T> instances = new HashSet<>();
            for(Class<?> implementationClass : implementationClasses) {
                instances.add(this.tryCreateFromImplementationClass(type, implementationClass));
            }
            return instances;
        }
        else {
            return this.tryLoadFromSPI(type);
        }
    }

    private <T> T tryCreateFromImplementationClass(Class<T> type, Class<?> implementationClass) throws ServiceDiscoveryException {
        if (type.isAssignableFrom(implementationClass)) {
            this.verifyAndStoreType(type, implementationClass);
            return this.loadServiceInstance(type, implementationClass);
        }
        else {
            throw new ServiceDiscoveryException("Implementation " + implementationClass.getName() + " is not assignable from type " + type.getName());
        }
    }

    private <T> Collection<Class<?>> tryLoadImplementationClass(Class<T> type) throws NoAvailableImplementationException {
        if (this.types.containsKey(type)) {
            return this.types.get(type);
        }
        else if (this.overrideDiscoveryFiles.containsKey(type.getName())) {
            Collection<String> resourceStrings = this.overrideDiscoveryFiles.get(type.getName());
            Set<Class<?>> classes = new HashSet<>();
            for(String resourceString : resourceStrings) {
                classes.add(this.tryLoadClassFromName(resourceString));
            }
            this.types.putAll(type, classes);
            return classes;
        }
        else {
            return Set.of();
        }
    }

    private <T> void verifyAndStoreType(Class<T> type, Class<?> implementationType) {
        this.verifyRegistration(type, implementationType);
        this.types.put(type, implementationType);
    }

    private Class<?> tryLoadClassFromName(String qualifiedName) throws NoAvailableImplementationException {
        for(ClassLoader classLoader : this.classLoaders) {
            try {
                return Class.forName(qualifiedName, true, classLoader);
            }
            catch (ClassNotFoundException e) {
                // Ignore, may be in another class loader
            }
        }
        throw new NoAvailableImplementationException("No implementation found for type " + qualifiedName);
    }

    private <T> Set<T> tryLoadFromSPI(Class<T> type) throws NoAvailableImplementationException {
        for(ClassLoader classLoader : this.classLoaders) {
            ServiceLoader<T> serviceLoader = this.getServiceLoader(type, classLoader);
            Set<? extends ServiceLoader.Provider<T>> providers = serviceLoader.stream().collect(Collectors.toSet());
            return providers.stream()
                    .map(ServiceLoader.Provider::get)
                    .collect(Collectors.toSet());
        }
        throw new NoAvailableImplementationException("No implementation found for type " + type.getName());
    }

    private <T> ServiceLoader<T> getServiceLoader(Class<T> type, ClassLoader classLoader) throws NoAvailableImplementationException {
        try {
            return ServiceLoader.load(type, classLoader);
        }
        catch(ServiceConfigurationError e) {
            throw new NoAvailableImplementationException("Cannot access service loader for type " + type.getName(), e);
        }
    }

    @SuppressWarnings("ReturnValueIgnored")
    private void verifyRegistration(Class<?> type, Class<?> implementation) {
        if (!type.isAssignableFrom(implementation)) {
            throw new IllegalArgumentException("Implementation " + implementation.getName() + " is not assignable from type " + type.getName());
        }
        try {
            // Ignore result, as we only want to check if the constructor is available
            implementation.getConstructor();
        }
        catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Implementation " + implementation.getName() + " does not have a default constructor");
        }
    }

    private <T> T loadServiceInstance(Class<T> type, Class<?> implementationClass) throws NoAvailableImplementationException {
        try {
            Constructor<?> constructor = implementationClass.getConstructor();

            constructor.setAccessible(true);
            Object instance = constructor.newInstance();
            constructor.setAccessible(false);

            return type.cast(instance);
        }
        catch (NoSuchMethodException e) {
            throw new NoAvailableImplementationException("Implementation " + implementationClass.getName() + " does not have a default constructor", e);
        }
        catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new NoAvailableImplementationException("Cannot instantiate implementation " + implementationClass.getName(), e);
        }
    }
}
