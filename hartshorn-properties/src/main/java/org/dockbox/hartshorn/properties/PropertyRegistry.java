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

import org.dockbox.hartshorn.properties.value.ValuePropertyParser;
import org.dockbox.hartshorn.util.option.Option;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A registry for {@link ConfiguredProperty} instances. This registry allows for the registration and retrieval of
 * properties by name.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public interface PropertyRegistry extends ObjectProperty {

    /**
     * Returns the property with the given key. If no properties are found with the given key, an empty {@link
     * ListProperty} is returned. This method will attempt to find all properties that start with the given key,
     * and create a new {@link ListProperty} from them.
     *
     * <p>If only a single value is found without an index (e.g. "key" instead of "key[0]"), the value will be
     * parsed using the provided {@link Function} to convert it to a list of values.
     *
     * @param name the key of the property to retrieve
     * @param singleValueMapper the function to use to map a single value to a list property
     * @return the property with the given key
     */
    Option<ListProperty> list(String name, Function<ValueProperty, ListProperty> singleValueMapper);

    /**
     * Returns all properties that match the given predicate. This will not convert the properties to a specific type,
     * and will instead return the raw {@link ConfiguredProperty} instances.
     *
     * @param predicate the predicate to match properties against
     * @return all properties that match the given predicate
     */
    List<ConfiguredProperty> find(Predicate<ConfiguredProperty> predicate);

    /**
     * Returns the value of the property with the given name. If no property is found with the given name, or the
     * property does not have a value, an empty {@link Option} is returned.
     *
     * @param name the name of the property to retrieve
     * @return the value of the property with the given name
     */
    default Option<String> value(String name) {
        return this.get(name).flatMap(ValueProperty::value);
    }

    /**
     * Returns the value of the property with the given name, parsed using the provided {@link ValuePropertyParser}.
     * If no property is found with the given name, or the property does not have a value, an empty {@link Option} is
     * returned.
     *
     * @param name the name of the property to retrieve
     * @param parser the parser to use to convert the property value
     * @param <T> the type to convert the value to
     *
     * @return the value of the property with the given name
     */
    default <T> Option<T> value(String name, ValuePropertyParser<T> parser) {
        return this.get(name).flatMap(parser::parse);
    }

    /**
     * Registers the given property in this registry.
     *
     * @param property the property to register
     */
    void register(ConfiguredProperty property);

    /**
     * Registers all properties in the given map in this registry.
     *
     * @param properties the properties to register
     */
    void registerAll(Map<String, ConfiguredProperty> properties);

    /**
     * Unregisters the property with the given name from this registry.
     *
     * @param name the name of the property to unregister
     */
    void unregister(String name);

    /**
     * Unregisters all properties from this registry.
     */
    void clear();

    /**
     * Returns whether the property with the given key exists in this map property. This method will check if a property
     * with the given key exists, or if any properties exist that start with the given key.
     *
     * @param name the key of the property to retrieve
     * @return the property with the given key, or an empty {@link Option}
     */
    boolean contains(String name);
}
