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

package org.dockbox.hartshorn.properties.loader;

import org.dockbox.hartshorn.properties.PropertyRegistry;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A loader for {@link PropertyRegistry} instances. This loader is used to populate a registry with
 * properties from a specific source.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
@FunctionalInterface
public interface PropertyRegistryLoader {

    /**
     * Loads the given {@link PropertyRegistry} with properties from the given {@link Path}.
     *
     * @param registry the registry to load
     * @param path the path to load properties from
     * @throws IOException if an error occurs while loading the properties
     */
    void loadRegistry(PropertyRegistry registry, Path path) throws IOException;
}
