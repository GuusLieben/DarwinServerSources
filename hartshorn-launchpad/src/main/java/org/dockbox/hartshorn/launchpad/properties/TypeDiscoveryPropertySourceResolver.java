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

package org.dockbox.hartshorn.launchpad.properties;

import org.dockbox.hartshorn.inject.component.ComponentContainer;
import org.dockbox.hartshorn.launchpad.environment.ApplicationEnvironment;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Property source resolver that discovers property sources based on the {@link PropertiesSource} annotation on
 * components within the application environment.
 *
 * @see PropertiesSource
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class TypeDiscoveryPropertySourceResolver implements PropertySourceResolver {

    private final ApplicationEnvironment environment;

    public TypeDiscoveryPropertySourceResolver(ApplicationEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public SequencedSet<String> resolve() {
        Collection<ComponentContainer<?>> containers = this.environment.componentRegistry().containers();
        return containers.stream()
                .map(ComponentContainer::type)
                .flatMap(type -> type.annotations().get(PropertiesSource.class).stream())
                .flatMap(source -> Stream.of(source.value()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
