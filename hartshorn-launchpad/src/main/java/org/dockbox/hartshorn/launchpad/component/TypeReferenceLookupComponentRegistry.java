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

package org.dockbox.hartshorn.launchpad.component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;

import org.dockbox.hartshorn.inject.annotations.Component;
import org.dockbox.hartshorn.inject.component.AnnotatedComponentContainer;
import org.dockbox.hartshorn.inject.component.ComponentContainer;
import org.dockbox.hartshorn.inject.component.ComponentRegistry;
import org.dockbox.hartshorn.launchpad.environment.ApplicationEnvironment;
import org.dockbox.hartshorn.launchpad.environment.EnvironmentTypeResolver;
import org.dockbox.hartshorn.util.CollectionUtilities;
import org.dockbox.hartshorn.util.introspect.annotations.AnnotationUtilities;
import org.dockbox.hartshorn.util.option.Option;

/**
 * {@link ComponentRegistry} implementation which recognizes classes annotated with {@link Component} (or compatible
 * stereotypes) as managed components. Classes are resolved through the given {@link EnvironmentTypeResolver}, which
 * is typically derived from the active {@link ApplicationEnvironment} through {@link
 * ApplicationEnvironment#typeResolver()}. Additional containers can be added to this registry through {@link 
 * #addCustomContainer(ComponentContainer)}.
 *
 * <p>Component resolution is cached, meaning this registry expects all required type resolution rules to be configured
 * before {@link #containers()} is first called. Note that custom component containers can always be added, and will be
 * taken into account when resolving all or specific component containers.
 *
 * @since 0.6.0
 *
 * @author Guus Lieben
 */
public class TypeReferenceLookupComponentRegistry implements ComponentRegistry {

    private final Set<ComponentContainer<?>> containers = new ConcurrentSkipListSet<>(ComponentContainer.COMPARE_BY_ID);
    private final EnvironmentTypeResolver typeResolver;
    private boolean environmentTypesResolved = false;

    public TypeReferenceLookupComponentRegistry(EnvironmentTypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    /**
     * Register the given container to the current registry
     * @param container
     */
    public void addCustomContainer(ComponentContainer<?> container) {
        withContainerCache(containers -> containers.add(container));
    }

    @Override
    public Collection<ComponentContainer<?>> containers() {
        return List.copyOf(this.withContainerCache(Function.identity()));
    }

    @Override
    public Option<ComponentContainer<?>> container(Class<?> type) {
        return withContainerCache(containers -> {
            List<ComponentContainer<?>> compatibleContainers = containers.stream()
                    .filter(container -> container.type().is(type))
                    .toList();
            if (compatibleContainers.size() > 1) {
                throw new IllegalStateException("Multiple compatible containers found for " + type);
            }
            return Option.of(compatibleContainers.isEmpty() ? null : compatibleContainers.getFirst());
        });
    }

    private <T> T withContainerCache(Function<Set<ComponentContainer<?>>, T> operator) {
        initializeCacheIfEmpty();
        return operator.apply(this.containers);
    }

    private void initializeCacheIfEmpty() {
        if (!environmentTypesResolved) {
            environmentTypesResolved = true;
            this.typeResolver.types(Component.class).stream()
                    // Filter out component stereotypes
                    .filter(type -> !AnnotationUtilities.isStereotypeOf(type.type(), Component.class))
                    .map(AnnotatedComponentContainer::new)
                    .forEach(this.containers::add);
        }
    }
}
