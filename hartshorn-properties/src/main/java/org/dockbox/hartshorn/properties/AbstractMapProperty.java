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
import org.dockbox.hartshorn.properties.loader.path.PropertyPathStyle;
import org.dockbox.hartshorn.properties.parse.support.ValueConfiguredPropertyParser;
import org.dockbox.hartshorn.properties.value.SimpleValueProperty;
import org.dockbox.hartshorn.properties.value.StandardValuePropertyParsers;
import org.dockbox.hartshorn.util.option.Option;
import org.dockbox.hartshorn.util.stream.EntryStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a property that contains a map of properties. This is used to represent nested properties in a
 * configuration.
 *
 * @param <T> the type of the key used to access the map
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public abstract class AbstractMapProperty<T> {

    private final Map<String, ConfiguredProperty> properties;
    private final PropertyPathStyle pathStyle;
    private final String name;

    protected AbstractMapProperty(String name, Map<String, ConfiguredProperty> properties, PropertyPathStyle pathStyle) {
        this.properties = new TreeMap<>(properties);
        this.name = name;
        this.pathStyle = pathStyle;
    }

    protected PropertyPathStyle pathStyle() {
        return this.pathStyle;
    }

    /**
     * Returns the properties of this map property.
     *
     * @return the properties of this map property
     */
    protected Map<String, ConfiguredProperty> properties() {
        return this.properties;
    }

    /**
     * Returns the name of this map property.
     *
     * @return the name of this map property
     */
    public String name() {
        return this.name;
    }

    /**
     * Returns the property with the given key. If the property does not exist, or cannot be represented as a
     * {@link ValueProperty} through the use of a {@link ValueConfiguredPropertyParser}, an empty {@link Option}
     * is returned.
     *
     * @param key the key of the property to retrieve
     * @return the property with the given key, or an empty {@link Option}
     */
    public Option<ValueProperty> get(T key) {
        return Option.of(this.properties.get(this.valueAccessor(key)))
                .flatMap(ValueConfiguredPropertyParser.INSTANCE::parse);
    }

    /**
     * Returns the accessor value for the given key. This is used to access the property in the map. Unlike {@link
     * #accessor(Object)}, this method should return the qualified key as if it were the start of a property name.
     * For example, if the key is "test", this method should return "test" instead of ".test".
     *
     * @param key the key to get the value for
     * @return the accessor value for the given key
     */
    protected abstract String valueAccessor(T key);

    /**
     * Returns the accessor value for the given key. This is used to access the property in the map. Unlike {@link
     * #valueAccessor(Object)}, this method should return the qualified key as if it were the end of a property name.
     * For example, if the key is "test", this method should return ".test" instead of "test".
     *
     * @param key the key to get the value for
     * @return the accessor value for the given key
     */
    protected abstract String accessor(T key);

    /**
     * Returns the property with the given key. If no properties are found with the given key, an empty {@link
     * ObjectProperty} is returned. This method will attempt to find all properties that start with the given key,
     * and create a new {@link ObjectProperty} from them.
     *
     * @param key the key of the property to retrieve
     * @return the property with the given key
     */
    public Option<ObjectProperty> object(T key) {
        Map<String, ConfiguredProperty> propertyMap = this.collectToMap(key, (name, property) -> {
            return name.startsWith(this.valueAccessor(key) + ".");
        }).entrySet().stream().collect(Collectors.toMap(
                // Strip trailing . from key
                entry -> entry.getKey().substring(1),
                Map.Entry::getValue
        ));
        ObjectProperty property = new MapObjectProperty(this.name() + this.accessor(key), propertyMap, this.pathStyle);
        return Option.of(property);
    }

    /**
     * Returns the property with the given key. If no properties are found with the given key, an empty {@link
     * ListProperty} is returned. This method will attempt to find all properties that start with the given key,
     * and create a new {@link ListProperty} from them.
     *
     * <p>If only a single value is found without an index (e.g. "key" instead of "key[0]"), the value will be
     * parsed using {@link StandardValuePropertyParsers#STRING_LIST} to convert it to a list of values.
     *
     * @param key the key of the property to retrieve
     * @return the property with the given key
     */
    public Option<ListProperty> list(T key) {
        return this.list(key, value -> {
            Option<String[]> values = StandardValuePropertyParsers.STRING_LIST.parse(value);
            List<String> valueList = values.stream().flatMap(Arrays::stream).toList();
            List<Property> listProperties = new ArrayList<>();
            for (int i = 0; i < valueList.size(); i++) {
                listProperties.add(i, new SimpleValueProperty(
                        this.name() + this.accessor(key) + this.pathStyle().index(i),
                        valueList.get(i)
                ));
            }
            return new SimpleListProperty(this.name() + this.accessor(key), listProperties);
        });
    }

    /**
     * Returns the property with the given key. If no properties are found with the given key, an empty {@link
     * ListProperty} is returned. This method will attempt to find all properties that start with the given key,
     * and create a new {@link ListProperty} from them.
     *
     * <p>If only a single value is found without an index (e.g. "key" instead of "key[0]"), the value will be
     * parsed using the provided {@link Function} to convert it to a list of values.
     *
     * @param key the key of the property to retrieve
     * @param singleValueMapper the function to use to map a single value to a list property
     * @return the property with the given key
     */
    public Option<ListProperty> list(T key, Function<ValueProperty, ListProperty> singleValueMapper) {
        if (this.properties().containsKey(this.valueAccessor(key))) {
            return this.get(key).map(singleValueMapper);
        } else {
            Map<String, ConfiguredProperty> propertyMap = this.collectToMap(key, (name, property) -> {
                return name.startsWith(this.valueAccessor(key) + "[");
            });
            ListProperty property = new MapListProperty(this.name() + this.accessor(key), propertyMap, this.pathStyle);
            return Option.of(property);
        }
    }

    /**
     * Returns whether the property with the given key exists in this map property. This method will check if a property
     * with the given key exists, or if any properties exist that start with the given key.
     *
     * @param key the key of the property to retrieve
     * @return the property with the given key, or an empty {@link Option}
     */
    public boolean contains(T key) {
        if (this.properties().containsKey(this.valueAccessor(key))) {
            return true;
        } else {
            String accessor = this.accessor(key);
            return this.properties().keySet().stream().anyMatch(propertyKey ->
                    // TODO: Delegate to implementation
                    propertyKey.startsWith(accessor + ".") || propertyKey.startsWith(accessor + "[")
            );
        }
    }

    /**
     * Returns all properties that match the given predicate. This will not convert the properties to a specific type,
     * and will instead return the raw {@link ConfiguredProperty} instances.
     *
     * @param predicate the predicate to match properties against
     * @return all properties that match the given predicate
     */
    protected List<ConfiguredProperty> find(BiPredicate<String, ConfiguredProperty> predicate) {
        return EntryStream.of(this.properties().entrySet())
                .filter(predicate)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Collects all properties that match the given predicate to a map. The key of the map is determined by the
     * implementation of {@link #key(Object, ConfiguredProperty)}.
     *
     * @param key the key to use in the key generation
     * @param predicate the predicate to match properties against
     * @return all properties that match the given predicate, collected to a map
     */
    protected Map<String, ConfiguredProperty> collectToMap(T key, BiPredicate<String, ConfiguredProperty> predicate) {
        return this.find(predicate).stream()
                .collect(Collectors.toMap(
                        property -> this.key(key, property),
                        Function.identity()
                ));
    }

    /**
     * Returns the relative key for the given property. The returned key is relative to the current property and
     * given prefix. For example, if the current property is "test" and the given property is "test.person.name" and
     * the prefix is "person", the returned key should be "name".
     *
     * @param prefix the prefix to use in the key generation
     * @param property the property to use in the key generation
     * @return the key for the given prefix and property
     */
    protected abstract String key(T prefix, ConfiguredProperty property);
}
