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

/**
 * Represents a property that holds a single value. This is the most basic form of a property.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public non-sealed interface ValueProperty extends Property {

    /**
     * Returns the value of this property.
     *
     * @return the value
     */
    Option<String> value();

    /**
     * Parses the value of this property using the provided parser.
     *
     * @param parser the parser to use
     * @return the parsed value
     * @param <T> the type to convert the value to
     */
    <T> Option<T> parse(ValuePropertyParser<T> parser);
}
