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

import org.dockbox.hartshorn.properties.list.SimpleListProperty;
import org.dockbox.hartshorn.properties.parse.support.ValueConfiguredPropertyParser;
import org.dockbox.hartshorn.properties.value.SimpleValueProperty;
import org.dockbox.hartshorn.properties.value.StandardValuePropertyParsers;
import org.dockbox.hartshorn.util.option.Option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractMapProperty<T> {

    private final Map<String, ConfiguredProperty> properties;
    private final String name;

    protected AbstractMapProperty(String name, Map<String, ConfiguredProperty> properties) {
        this.properties = new TreeMap<>(properties);
        this.name = name;
    }

    protected Map<String, ConfiguredProperty> properties() {
        return this.properties;
    }

    public String name() {
        return this.name;
    }

    public Option<ValueProperty> get(T key) {
        return Option.of(this.properties.get(valueAccessor(key)))
                .flatMap(ValueConfiguredPropertyParser.INSTANCE::parse);
    }

    protected abstract String valueAccessor(T key);

    public Option<ObjectProperty> object(T key) {
        Map<String, ConfiguredProperty> propertyMap = this.collectToMap(key, (name, property) -> {
            return name.startsWith(this.accessor(key) + ".");
        }).entrySet().stream().collect(Collectors.toMap(
                // Strip trailing . from key
                entry -> entry.getKey().substring(1),
                Map.Entry::getValue
        ));
        ObjectProperty property = new MapObjectProperty(this.name() + this.accessor(key), propertyMap);
        return Option.of(property);
    }

    protected abstract String accessor(T key);

    public Option<ListProperty> list(T key) {
        return this.list(key, value -> {
            Option<String[]> values = StandardValuePropertyParsers.STRING_LIST.parse(value);
            List<String> valueList = values.stream().flatMap(Arrays::stream).toList();
            List<Property> listProperties = new ArrayList<>();
            for (int i = 0; i < valueList.size(); i++) {
                listProperties.add(i, new SimpleValueProperty(this.name() + this.accessor(key) + "[" + i + "]", valueList.get(i)));
            }
            return new SimpleListProperty(this.name() + this.accessor(key), listProperties);
        });
    }

    public Option<ListProperty> list(T key, Function<ValueProperty, ListProperty> singleValueMapper) {
        if (this.contains(key)) {
            return this.get(key).map(singleValueMapper);
        } else {
            Map<String, ConfiguredProperty> propertyMap = this.collectToMap(key, (name, property) -> {
                return name.startsWith(key + "[");
            });
            ListProperty property = new MapListProperty(this.name() + this.accessor(key), propertyMap);
            return Option.of(property);
        }
    }

    public boolean contains(T key) {
        if (this.properties().containsKey(this.valueAccessor(key))) {
            return true;
        } else {
            String accessor = this.accessor(key);
            return this.properties().keySet().stream().anyMatch(propertyKey ->
                    propertyKey.startsWith(accessor + ".") || propertyKey.startsWith(accessor + "[")
            );
        }
    }

    public List<ConfiguredProperty> find(BiPredicate<String, ConfiguredProperty> predicate) {
        return this.properties().entrySet().stream()
                .filter(entry -> predicate.test(entry.getKey(), entry.getValue()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private Map<String, ConfiguredProperty> collectToMap(T key, BiPredicate<String, ConfiguredProperty> predicate) {
        return this.find(predicate).stream()
                .collect(Collectors.toMap(
                        property -> this.key(key, property),
                        Function.identity()
                ));
    }

    protected abstract String key(T prefix, ConfiguredProperty property);
}
