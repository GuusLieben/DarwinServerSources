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

import org.dockbox.hartshorn.properties.list.ListPropertyParser;
import org.dockbox.hartshorn.util.option.Option;

import java.util.Collection;
import java.util.Map;

public class MapListProperty extends AbstractMapProperty<Integer> implements ListProperty {

    public MapListProperty(String name, Map<String, ConfiguredProperty> properties) {
        super(name, properties);
    }

    @Override
    protected String valueAccessor(Integer key) {
        return "[" + key + "]";
    }

    @Override
    protected String accessor(Integer key) {
        return "[" + key + "]";
    }

    @Override
    protected String key(Integer prefix, ConfiguredProperty property) {
        return property.name().substring(this.name().length() + this.accessor(prefix).length());
    }

    @Override
    public int size() {
        return this.properties().keySet().stream()
                .map(key -> key.split("\\[")[1].split("]")[0])
                .map(Integer::parseInt)
                .max(Integer::compareTo)
                .map(i -> i + 1) // Add one to get the size, as the max index is zero-based
                .orElse(0);
    }

    @Override
    public Option<ValueProperty> get(int index) {
        return this.get(Integer.valueOf(index));
    }

    @Override
    public Option<ObjectProperty> object(int index) {
        return this.object(Integer.valueOf(index));
    }

    @Override
    public Option<ListProperty> list(int index) {
        return this.list(Integer.valueOf(index));
    }

    @Override
    public <T> Collection<T> parse(ListPropertyParser<T> parser) {
        return parser.parse(this);
    }
}
