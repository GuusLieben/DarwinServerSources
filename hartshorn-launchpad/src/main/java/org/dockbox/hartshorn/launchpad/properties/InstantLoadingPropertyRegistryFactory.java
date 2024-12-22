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

import org.dockbox.hartshorn.properties.MapPropertyRegistry;
import org.dockbox.hartshorn.properties.PropertyRegistry;
import org.dockbox.hartshorn.properties.loader.PropertyRegistryLoader;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.SequencedSet;

/**
 * Factory for creating {@link PropertyRegistry} instances that are loaded immediately after the registry is created,
 * and thus before it is released to the caller.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class InstantLoadingPropertyRegistryFactory implements PropertyRegistryFactory {

    private final PropertyRegistryLoader propertyRegistryLoader;

    public InstantLoadingPropertyRegistryFactory(PropertyRegistryLoader propertyRegistryLoader) {
        this.propertyRegistryLoader = propertyRegistryLoader;
    }

    /**
     * Creates a new {@link PropertyRegistry} instance.
     *
     * @return the created registry
     */
    protected PropertyRegistry createRegistry() {
        return new MapPropertyRegistry();
    }

    @Override
    public PropertyRegistry createRegistry(SequencedSet<URI> sources) throws IOException {
        PropertyRegistry propertyRegistry = this.createRegistry();
        for(URI resource : sources) {
            this.propertyRegistryLoader.loadRegistry(propertyRegistry, Path.of(resource));
        }
        return propertyRegistry;
    }
}
