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

package org.dockbox.hartshorn.properties;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class MapPropertyRegistry extends MapObjectProperty implements PropertyRegistry {

    public MapPropertyRegistry() {
        this("");
    }

    public MapPropertyRegistry(String name) {
        super(name, Map.of());
    }

    @Override
    public List<ConfiguredProperty> find(Predicate<ConfiguredProperty> predicate) {
        return this.find((name, property) -> predicate.test(property));
    }

    @Override
    public void register(ConfiguredProperty property) {
        if (this.contains(property.name())) {
            throw new IllegalArgumentException("Property with name " + property.name() + " already exists. If you intended to load a property with multiple values, implement the appropriate ConfiguredProperty");
        }
        this.properties().put(property.name(), property);
    }

    @Override
    public void registerAll(Map<String, ConfiguredProperty> properties) {
        this.properties().putAll(properties);
    }

    @Override
    public void unregister(String name) {
        this.properties().remove(name);
    }

    @Override
    public void clear() {
        this.properties().clear();
    }
}
