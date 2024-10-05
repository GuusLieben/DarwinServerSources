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

package org.dockbox.hartshorn.launchpad.launch;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.dockbox.hartshorn.inject.processing.ComponentProcessorRegistry;
import org.dockbox.hartshorn.inject.processing.HierarchicalBinderPostProcessor;
import org.dockbox.hartshorn.inject.processing.HierarchicalBinderProcessorRegistry;
import org.dockbox.hartshorn.launchpad.ApplicationContext;
import org.dockbox.hartshorn.launchpad.ProcessableApplicationContext;
import org.dockbox.hartshorn.inject.processing.ComponentPostProcessor;
import org.dockbox.hartshorn.inject.processing.ComponentPreProcessor;
import org.dockbox.hartshorn.inject.processing.ComponentProcessor;
import org.dockbox.hartshorn.launchpad.activation.ServiceActivator;
import org.dockbox.hartshorn.inject.graph.support.ComponentInitializationException;
import org.dockbox.hartshorn.launchpad.activation.ServiceActivatorCollector;
import org.dockbox.hartshorn.util.CollectionUtilities;
import org.dockbox.hartshorn.util.introspect.Introspector;

/**
 * Registers component processors to the application context. This class should be used to prepare the application
 * context before {@link ProcessableApplicationContext#loadContext()} is called, to ensure that all components are
 * processed correctly.
 *
 * @see ComponentProcessor
 * @see ServiceActivator
 * @see ApplicationContext
 * @see ProcessableApplicationContext
 *
 * @since 0.6.0
 *
 * @author Guus Lieben
 */
public class ComponentProcessorRegistrar {

    private final Set<ComponentProcessor> additionalComponentProcessors = new HashSet<>();
    private final Set<HierarchicalBinderPostProcessor> additionalBinderProcessors = new HashSet<>();

    private final ServiceActivatorCollector activatorCollector;
    private final ApplicationBuildContext buildContext;

    public ComponentProcessorRegistrar(ServiceActivatorCollector activatorCollector, ApplicationBuildContext buildContext) {
        this.activatorCollector = activatorCollector;
        this.buildContext = buildContext;
    }

    /**
     * Adds additional component processors to the registrar. These processors will be registered to the application
     * context when {@link #registerComponentProcessors(ComponentProcessorRegistry, Introspector, Set)} is called.
     *
     * @param processors the additional processors to add
     */
    public void withAdditionalComponentProcessors(Collection<? extends ComponentProcessor> processors) {
        this.additionalComponentProcessors.addAll(processors);
    }

    public void withAdditionalBinderProcessors(Collection<? extends HierarchicalBinderPostProcessor> processors) {
        this.additionalBinderProcessors.addAll(processors);
    }


    /**
     * Registers component processors to the application context. This method will collect all activators from the given
     * set of annotations, and then collect all processors from the activators. The processors will then be registered
     * to the application context.
     *
     * @param registry the application context
     * @param introspector the introspector to use for constructor lookup
     * @param activators the set of annotations to collect activators from
     */
    public void registerComponentProcessors(ComponentProcessorRegistry registry, Introspector introspector, Set<Annotation> activators) {
        Set<ServiceActivator> serviceActivatorAnnotations = activators.stream()
            .flatMap(activator -> this.activatorCollector.collectDeclarationsOnActivator(activator).stream())
            .collect(Collectors.toSet());

        Set<Class<? extends ComponentPreProcessor>> preProcessorTypes = this.resolveComponentPreProcessorTypes(serviceActivatorAnnotations);
        Set<Class<? extends ComponentPostProcessor>> postProcessorTypes = this.resolveComponentPostProcessorTypes(serviceActivatorAnnotations);

        if (this.buildContext.logger().isDebugEnabled()) {
            int totalProcessors = preProcessorTypes.size() + postProcessorTypes.size();
            this.buildContext.logger().debug("Registering {} component processors to application context", totalProcessors);
        }

        this.registerPostProcessors(registry, postProcessorTypes);
        this.registerPreProcessors(registry, introspector, preProcessorTypes);
    }

    /**
     * Registers binder processors to the application context. This method will collect all activators from the given
     * set of annotations, and then collect all processors from the activators. The processors will then be registered
     * to the application context.
     *
     * @param registry the registry to register the binder processors to
     * @param introspector the introspector to use for constructor lookup
     * @param activators the set of annotations to collect activators from
     */
    public void registerBinderProcessors(HierarchicalBinderProcessorRegistry registry, Introspector introspector, Set<Annotation> activators) {
        Set<ServiceActivator> serviceActivatorAnnotations = activators.stream()
            .flatMap(activator -> this.activatorCollector.collectDeclarationsOnActivator(activator).stream())
            .collect(Collectors.toSet());

        Set<Class<? extends HierarchicalBinderPostProcessor>> binderPostProcessorTypes = this.resolveBinderPostProcessorTypes(serviceActivatorAnnotations);

        if (this.buildContext.logger().isDebugEnabled()) {
            this.buildContext.logger().debug("Registering {} binder processors to application context", binderPostProcessorTypes.size());
        }

        this.registerBinderPostProcessors(registry, introspector, binderPostProcessorTypes);
    }

    protected Set<Class<? extends HierarchicalBinderPostProcessor>> resolveBinderPostProcessorTypes(
            Set<ServiceActivator> serviceActivatorAnnotations) {
        return this.resolveProcessorTypes(serviceActivatorAnnotations, HierarchicalBinderPostProcessor.class, ServiceActivator::binderPostProcessors);
    }

    protected Set<Class<? extends ComponentPreProcessor>> resolveComponentPreProcessorTypes(
            Set<ServiceActivator> serviceActivatorAnnotations) {
        return this.resolveProcessorTypes(serviceActivatorAnnotations, ComponentPreProcessor.class, ServiceActivator::componentPreProcessors);
    }

    protected Set<Class<? extends ComponentPostProcessor>> resolveComponentPostProcessorTypes(
            Set<ServiceActivator> serviceActivatorAnnotations) {
        return this.resolveProcessorTypes(serviceActivatorAnnotations, ComponentPostProcessor.class, ServiceActivator::componentPostProcessors);
    }

    private <T> Set<Class<? extends T>> resolveProcessorTypes(Set<ServiceActivator> serviceActivators, Class<T> processorType, Function<ServiceActivator, Class<? extends T>[]> lookup) {
        Set<Class<? extends T>> processorsFromActivator = CollectionUtilities
                .flatMapArray(serviceActivators, lookup)
                .collect(Collectors.toSet());

        // Compatibility with old activator attribute, to be removed in 0.7.0
        @Deprecated(since = "0.7.0", forRemoval = true)
        Set<Class<? extends ComponentProcessor>> processorsFromOldActivatorAttribute = CollectionUtilities
                .flatMapArray(serviceActivators, ServiceActivator::processors)
                .collect(Collectors.toSet());
        Set<Class<? extends T>> deprecatedNotationProcessors = extractProcessors(processorsFromOldActivatorAttribute, processorType);

        return CollectionUtilities.merge(processorsFromActivator, deprecatedNotationProcessors);
    }

    /**
     * Registers post-processors to the application context. Post-processors are registered as-is, as they are not
     * required to be instantiated before they are used. If additional processors are registered, they will be added to
     * the set of post-processors.
     *
     * @param registry the application context
     * @param processorTypes the types of post-processors to register
     *
     * @see ComponentProcessorRegistry#registryLazy(Class)
     * @see ComponentProcessorRegistry#register(ComponentProcessor)
     */
    protected void registerPostProcessors(ComponentProcessorRegistry registry, Set<Class<? extends ComponentPostProcessor>> processorTypes) {
        for (Class<? extends ComponentPostProcessor> postProcessorType : processorTypes) {
            registry.registryLazy(postProcessorType);
        }

        this.additionalComponentProcessors.stream()
                .filter(ComponentPostProcessor.class::isInstance)
                .map(ComponentPostProcessor.class::cast)
                .forEach(registry::register);
    }

    /**
     * Registers pre-processors to the application context. Pre-processors are instantiated before they are used, as they
     * are required to process components before they are registered.
     *
     * @param registry the application context
     * @param introspector the introspector to use for constructor lookup
     * @param processorTypes the types of pre-processors to register
     *
     * @see ComponentProcessorRegistry#register(ComponentProcessor)
     */
    protected void registerPreProcessors(ComponentProcessorRegistry registry, Introspector introspector, Set<Class<? extends ComponentPreProcessor>> processorTypes) {
        Set<ComponentPreProcessor> componentProcessors = this.createPreProcessors(introspector, ComponentPreProcessor.class, processorTypes);
        for (ComponentProcessor componentProcessor : componentProcessors) {
            registry.register(componentProcessor);
        }
    }

    /**
     * Registers binder post-processors to the application context. Binder post-processors are instantiated before they
     * are used, as they are required to process binders the moment they are registered.
     *
     * @param registry the registry containing the binder post-processors
     * @param introspector the introspector to use for constructor lookup
     * @param processorTypes the types of binder post-processors to register
     */
    protected void registerBinderPostProcessors(
            HierarchicalBinderProcessorRegistry registry,
            Introspector introspector,
            Set<Class<? extends HierarchicalBinderPostProcessor>> processorTypes) {
        Collection<HierarchicalBinderPostProcessor> processors = new HashSet<>(
            this.createPreProcessors(introspector, HierarchicalBinderPostProcessor.class, processorTypes)
        );
        processors.addAll(this.additionalBinderProcessors);
        processors.forEach(registry::register);
    }

    protected <T> Set<Class<? extends T>> extractProcessors(Collection<Class<? extends ComponentProcessor>> processorTypes, Class<T> processorClass) {
        //noinspection unchecked
        return processorTypes.stream()
            .filter(processorClass::isAssignableFrom)
            .map(type -> (Class<? extends T>) type)
            .collect(Collectors.toSet());
    }

    /**
     * Instantiates required pre-processors so they can be registered for immediate use. If additional processors are
     * registered, they will be added to the set of pre-processors.
     *
     * @param introspector the introspector to use for constructor lookup
     * @param processorTypes the types of pre-processors to instantiate
     * @return a set of pre-processors
     */
    @NonNull
    protected <T> Set<T> createPreProcessors(Introspector introspector, Class<T> rootType, Set<Class<? extends T>> processorTypes) {
        Set<T> componentProcessors = this.additionalComponentProcessors
            .stream()
            .filter(rootType::isInstance)
            .map(rootType::cast)
            .collect(Collectors.toSet());

        // Note: pre-processors should never have dependencies, as they are used to process components before they are registered
        // and therefore cannot rely on other components being available
        for (Class<? extends T> processorType : processorTypes) {
            var constructor = introspector.introspect(processorType)
                    .constructors()
                    .defaultConstructor();
            if (constructor.absent()) {
                throw new ComponentInitializationException("Processor %s does not have a default constructor, skipping".formatted(processorType.getSimpleName()));
            }
            try {
                componentProcessors.add(constructor.get().create());
            }
            catch(Throwable e) {
                throw new ComponentInitializationException("Failed to create processor", e);
            }
        }
        return componentProcessors;
    }
}
