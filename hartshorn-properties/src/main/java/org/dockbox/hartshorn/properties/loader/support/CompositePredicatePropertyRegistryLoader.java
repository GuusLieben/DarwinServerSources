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

package org.dockbox.hartshorn.properties.loader.support;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.dockbox.hartshorn.properties.PropertyRegistry;
import org.dockbox.hartshorn.properties.loader.PredicatePropertyRegistryLoader;

public class CompositePredicatePropertyRegistryLoader implements PredicatePropertyRegistryLoader {

    private final Set<PredicatePropertyRegistryLoader> loaders = new HashSet<>();

    public void addLoader(PredicatePropertyRegistryLoader loader) {
        this.loaders.add(loader);
    }

    public void removeLoader(PredicatePropertyRegistryLoader loader) {
        this.loaders.remove(loader);
    }

    public Set<PredicatePropertyRegistryLoader> loaders() {
        return this.loaders;
    }

    @Override
    public boolean isCompatible(Path path) {
        return this.loaders.stream().anyMatch(loader -> loader.isCompatible(path));
    }

    @Override
    public void loadRegistry(PropertyRegistry registry, Path path) throws IOException {
        for(PredicatePropertyRegistryLoader loader : this.loaders) {
            if(loader.isCompatible(path)) {
                loader.loadRegistry(registry, path);
            }
        }
    }
}
