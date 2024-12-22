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

package org.dockbox.hartshorn.properties.parse;

import org.dockbox.hartshorn.properties.ConfiguredProperty;
import org.dockbox.hartshorn.util.option.Option;

/**
 * A parser for {@link ConfiguredProperty} instances. This parser is used to convert the value of a
 * {@link ConfiguredProperty} to a specific type.
 *
 * @param <T> the type to convert the value to
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
@FunctionalInterface
public interface ConfiguredPropertyParser<T> {

    /**
     * Parses the value of the given {@link ConfiguredProperty} to the type of this parser. If the
     * value cannot be parsed, an empty {@link Option} is returned.
     *
     * @param property the property to parse
     * @return the parsed value, or an empty {@link Option}
     */
    Option<T> parse(ConfiguredProperty property);
}
