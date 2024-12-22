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

package org.dockbox.hartshorn.launchpad.environment;

import org.dockbox.hartshorn.inject.graph.support.ComponentInitializationException;
import org.dockbox.hartshorn.launchpad.properties.InstantLoadingPropertyRegistryFactory;
import org.dockbox.hartshorn.launchpad.properties.PropertyRegistryFactory;
import org.dockbox.hartshorn.launchpad.properties.PropertySourceResolver;
import org.dockbox.hartshorn.launchpad.resources.ResourceLookup;
import org.dockbox.hartshorn.properties.PropertyRegistry;
import org.dockbox.hartshorn.properties.loader.PredicatePropertyRegistryLoader;
import org.dockbox.hartshorn.properties.loader.PropertyRegistryLoader;
import org.dockbox.hartshorn.properties.loader.support.CompositePredicatePropertyRegistryLoader;
import org.dockbox.hartshorn.spi.DiscoveryService;
import org.dockbox.hartshorn.spi.ServiceDiscoveryException;
import org.dockbox.hartshorn.util.ApplicationRuntimeException;
import org.dockbox.hartshorn.util.CollectionUtilities;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Factory for creating {@link PropertyRegistry} instances based on a collection of {@link PropertySourceResolver}s. The
 * sources are resolved and loaded into the registry using a {@link PropertyRegistryLoader}, which may be composed of
 * multiple loaders. The {@link PropertyRegistryLoader} instances are resolved from SPI providers, allowing for
 * extensibility.
 *
 * @see PropertyRegistryLoader
 * @see PropertySourceResolver
 * @see ResourceLookup
 * @see PropertyRegistry
 * @see InstantLoadingPropertyRegistryFactory
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class EnvironmentPropertyRegistryFactory {

    public PropertyRegistry createRegistry(Collection<PropertySourceResolver> propertySourceResolvers, ResourceLookup resourceLookup) {
        Set<PropertyRegistryLoader> propertyRegistryLoaders = this.resolveRegistryLoaders();
        PropertyRegistryLoader propertyRegistryLoader = this.createRegistryLoader(propertyRegistryLoaders);
        PropertyRegistryFactory propertyRegistryFactory = new InstantLoadingPropertyRegistryFactory(propertyRegistryLoader);
        try {
            SequencedSet<URI> resources = this.resolveResources(propertySourceResolvers, resourceLookup);
            return propertyRegistryFactory.createRegistry(resources);
        }
        catch(IOException e) {
            throw new ComponentInitializationException("Could not initialize PropertyRegistry", e);
        }
    }

    private SequencedSet<URI> resolveResources(Collection<PropertySourceResolver> propertySourceResolvers, ResourceLookup resourceLookup) {
        return propertySourceResolvers.stream()
                .flatMap(resolver -> resolver.resolve().stream())
                .flatMap(source -> resourceLookup.lookup(source).stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<PropertyRegistryLoader> resolveRegistryLoaders() {
        Set<PropertyRegistryLoader> propertyRegistryLoaders;
        try {
            propertyRegistryLoaders = DiscoveryService.instance().discoverAll(PropertyRegistryLoader.class);
        }
        catch(ServiceDiscoveryException e) {
            throw new ComponentInitializationException("Failed to initialize PropertyRegistryLoaders", e);
        }
        return propertyRegistryLoaders;
    }

    private PropertyRegistryLoader createRegistryLoader(Collection<PropertyRegistryLoader> propertyRegistryLoaders) {
        PropertyRegistryLoader propertyRegistryLoader;
        if (propertyRegistryLoaders.size() == 1) {
            propertyRegistryLoader = CollectionUtilities.first(propertyRegistryLoaders);
        }
        else {
            CompositePredicatePropertyRegistryLoader composite = new CompositePredicatePropertyRegistryLoader();
            for(PropertyRegistryLoader registryLoader : propertyRegistryLoaders) {
                if (registryLoader instanceof PredicatePropertyRegistryLoader predicateLoader) {
                    composite.addLoader(predicateLoader);
                }
                else {
                    throw new ApplicationRuntimeException("Found multiple PropertyRegistryLoaders, but cannot differentiate between them. "
                            + "Please implement PredicatePropertyRegistryLoader for " + registryLoader.getClass().getName());
                }
            }
            propertyRegistryLoader = composite;
        }
        return propertyRegistryLoader;
    }
}
