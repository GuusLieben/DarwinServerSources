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

/**
 * Represents a property that contains other properties, accessed by a key.
 *
 * @see Property
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public non-sealed interface ObjectProperty extends Property {

    /**
     * Returns the keys of the properties contained in this object property.
     *
     * @return the keys of the properties contained in this object property
     */
    List<String> keys();

    /**
     * Returns the property with the given name. If the property does not exist, or cannot be represented as a
     * {@link ValueProperty}, an empty {@link Option} is returned.
     *
     * @param name the name of the property to retrieve
     * @return the property with the given name, or an empty {@link Option}
     */
    Option<ValueProperty> get(String name);

    /**
     * Returns the property with the given name. If the property does not exist, or cannot be represented as a
     * {@link ObjectProperty}, an empty {@link Option} is returned.
     *
     * @param name the name of the property to retrieve
     * @return the property with the given name, or an empty {@link Option}
     */
    Option<ObjectProperty> object(String name);

    /**
     * Returns the property with the given name. If the property does not exist, or cannot be represented as a
     * {@link ListProperty}, an empty {@link Option} is returned.
     *
     * @param name the name of the property to retrieve
     * @return the property with the given name, or an empty {@link Option}
     */
    Option<ListProperty> list(String name);

    /**
     * Parses the current object property using the provided parser. If the parser is unable to parse the property,
     * an empty {@link Option} is returned.
     *
     * @param parser the parser to use
     * @param <T> the type to parse the property to
     *
     * @return the parsed property, or an empty {@link Option}
     */
    <T> Option<T> parse(ObjectPropertyParser<T> parser);
}
