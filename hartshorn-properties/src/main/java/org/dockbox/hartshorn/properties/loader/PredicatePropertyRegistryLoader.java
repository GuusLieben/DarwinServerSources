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

import java.nio.file.Path;

/**
 * A {@link PropertyRegistryLoader} that can be tested for compatibility with a given path before loading
 * the registry.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public interface PredicatePropertyRegistryLoader extends PropertyRegistryLoader {

    /**
     * Tests if the given path is compatible with this loader. If this method returns {@code true}, the loader
     * can be used to load the registry. If it returns {@code false}, the loader should not be used with the
     * given path.
     *
     * @param path the path to test
     * @return {@code true} if the loader is compatible with the given path, {@code false} otherwise
     */
    boolean isCompatible(Path path);
}
