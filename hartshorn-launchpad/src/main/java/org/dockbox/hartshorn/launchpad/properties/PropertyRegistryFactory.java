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

import org.dockbox.hartshorn.properties.PropertyRegistry;

import java.io.IOException;
import java.net.URI;
import java.util.SequencedSet;

/**
 * Factory for creating {@link PropertyRegistry} instances.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
@FunctionalInterface
public interface PropertyRegistryFactory {

    /**
     * Creates a {@link PropertyRegistry} instance based on the provided sources. Sources will be loaded into the
     * registry in the order they are provided.
     *
     * @param sources the sources to load into the registry
     * @return the created registry
     * @throws IOException when an error occurs while loading the sources
     */
    PropertyRegistry createRegistry(SequencedSet<URI> sources) throws IOException;
}
