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

import org.dockbox.hartshorn.properties.parse.ConfiguredPropertyParser;
import org.dockbox.hartshorn.util.option.Option;

/**
 * Represents a single property that is configured. This is the most basic form of a property, containing only a name
 * and a value. Configured properties are typically registered to {@link PropertyRegistry property registries}, which
 * can be used to retrieve the properties as any supported {@link Property} type.
 *
 * @see Property
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public interface ConfiguredProperty {

    /**
     * Returns the full name of the property. This name is typically used to identify the property in a registry,
     * and is thus the fully qualified name of the property (e.g. {@code "org.dockbox.hartshorn.properties.example"}).
     *
     * @return the name of the property
     */
    String name();

    /**
     * Returns the value of the property. As properties are raw values, this is always a {@link String}. To convert
     * the value to a specific type, use {@link #value(ConfiguredPropertyParser)}.
     *
     * @return the value of the property
     */
    Option<String> value();

    /**
     * Converts the value of this property to an instance of the type {@link T} using the provided {@link ConfiguredPropertyParser}.
     *
     * @param parser the parser to use
     * @param <T> the type to convert the value to
     *
     * @return an {@link Option} containing the parsed value, or an empty {@link Option}
     */
    <T> Option<T> value(ConfiguredPropertyParser<T> parser);
}
