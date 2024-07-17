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

import org.dockbox.hartshorn.util.option.Option;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MapPropertyRegistry implements PropertyRegistry {

    private final Map<String, ConfiguredProperty> properties = new TreeMap<>();

    @Override
    public List<String> keys() {
        return List.copyOf(properties.keySet());
    }

    @Override
    public Option<ConfiguredProperty> get(String name) {
        ConfiguredProperty property = properties.get(name);
        return Option.of(property);
    }

    @Override
    public void register(ConfiguredProperty property) {
        if (this.contains(property.name())) {
            throw new IllegalArgumentException("Property with name " + property.name() + " already exists. If you intended to load a property with multiple values, implement the appropriate ConfiguredProperty");
        }
        this.properties.put(property.name(), property);
    }

    @Override
    public void unregister(String name) {
        this.properties.remove(name);
    }

    @Override
    public void clear() {
        this.properties.clear();
    }

    @Override
    public boolean contains(String name) {
        return this.properties.containsKey(name);
    }
}
