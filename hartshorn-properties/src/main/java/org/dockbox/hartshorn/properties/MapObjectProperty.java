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

import org.dockbox.hartshorn.properties.object.ObjectPropertyParser;
import org.dockbox.hartshorn.util.option.Option;

import java.util.List;
import java.util.Map;

public class MapObjectProperty extends AbstractMapProperty<String> implements ObjectProperty {

    public MapObjectProperty(String name, Map<String, ConfiguredProperty> properties) {
        super(name, properties);
    }

    @Override
    protected String valueAccessor(String key) {
        return key;
    }

    @Override
    protected String accessor(String key) {
        return this.name().isBlank()
                ? key
                : "." + key;
    }

    @Override
    protected String key(String prefix, ConfiguredProperty property) {
        // Remove prefix from name
        return property.name().substring(this.name().length() + this.accessor(prefix).length());
    }

    @Override
    public List<String> keys() {
        return this.properties().keySet().stream()
                .map(key -> key.split("\\.")[0])
                .distinct()
                .toList();
    }

    @Override
    public <T> Option<T> parse(ObjectPropertyParser<T> parser) {
        return parser.parse(this);
    }
}
