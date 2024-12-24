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

package org.dockbox.hartshorn.properties.list;

import org.dockbox.hartshorn.properties.ListProperty;

import java.util.Collection;

/**
 * A parser to convert {@link ListProperty} instances to instances of a specific type.
 *
 * @param <T> the type to convert the value to
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
@FunctionalInterface
public interface ListPropertyParser<T> {

    /**
     * Parses the given {@link ListProperty} to a collection of instances of the target type. If the conversion fails,
     * an empty collection is returned.
     *
     * @param property the property to parse
     * @return the parsed values
     */
    Collection<T> parse(ListProperty property);
}
