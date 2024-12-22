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
import org.dockbox.hartshorn.properties.loader.path.PropertyPathStyle;
import org.dockbox.hartshorn.util.option.Option;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A {@link MapListProperty} is a {@link AbstractMapProperty} that represents a list of properties. The keys of the
 * properties are formatted indexes.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class MapListProperty extends AbstractMapProperty<Integer> implements ListProperty {

    public MapListProperty(String name, Map<String, ConfiguredProperty> properties, PropertyPathStyle pathStyle) {
        super(name, properties, pathStyle);
    }

    @Override
    protected String valueAccessor(Integer key) {
        return this.pathStyle().index(key);
    }

    @Override
    protected String accessor(Integer key) {
        return this.pathStyle().index(key);
    }

    @Override
    protected String key(Integer prefix, ConfiguredProperty property) {
        return property.name().substring(this.name().length() + this.accessor(prefix).length());
    }

    @Override
    public int size() {
        return this.properties().keySet().stream()
                .map(key -> this.pathStyle().resolveIndexes(key)[0])
                .map(Integer::parseInt)
                .max(Integer::compareTo)
                .map(index -> index + 1) // Add one to get the size, as the max index is zero-based
                .orElse(0);
    }

    @Override
    public Option<ValueProperty> get(int index) {
        return this.get(Integer.valueOf(index));
    }

    @Override
    public List<ValueProperty> values() {
        List<ValueProperty> properties = new ArrayList<>();
        for (int i = 0; i < this.size(); i++) {
            this.get(i).peek(properties::add);
        }
        return properties;
    }

    @Override
    public Option<ObjectProperty> object(int index) {
        return this.object(Integer.valueOf(index));
    }

    @Override
    public List<ObjectProperty> objects() {
        List<ObjectProperty> properties = new ArrayList<>();
        for (int i = 0; i < this.size(); i++) {
            this.object(i).peek(properties::add);
        }
        return properties;
    }

    @Override
    public Option<ListProperty> list(int index) {
        return this.list(Integer.valueOf(index));
    }

    @Override
    public List<ListProperty> lists() {
        List<ListProperty> properties = new ArrayList<>();
        for (int i = 0; i < this.size(); i++) {
            this.list(i).peek(properties::add);
        }
        return properties;
    }

    @Override
    public <T> Collection<T> parse(ListPropertyParser<T> parser) {
        return parser.parse(this);
    }
}
