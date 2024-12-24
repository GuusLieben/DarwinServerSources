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
import java.util.List;

/**
 * Represents a property that is a list of values. The values represented by this list can be any {@link Property},
 * including other {@link ListProperty} instances. As such values are not required to take the same form, this
 * interface provides methods to retrieve values as {@link ValueProperty}, {@link ObjectProperty} and {@link
 * ListProperty} instances.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public non-sealed interface ListProperty extends Property {

    /**
     * Returns the size of the list. This number is not necessarily equal to the number of elements that
     * are actually present in the list. The size is determined by the highest index that is present in
     * the list.
     *
     * @return The number of values in this list
     */
    int size();

    /**
     * Returns the value at the specified index as a {@link ValueProperty}. If the value at the specified index is not
     * present, or cannot be represented as a {@link ValueProperty}, an empty {@link Option} is returned.
     *
     * @param index The index of the value to retrieve
     * @return The value at the specified index, or an empty {@link Option}
     */
    Option<ValueProperty> get(int index);

    /**
     * Returns all values in this list as a collection of {@link ValueProperty} instances.
     *
     * @return All values in this list
     */
    List<ValueProperty> values();

    /**
     * Returns the value at the specified index as an {@link ObjectProperty}. If the value at the specified index is not
     * present, or cannot be represented as an {@link ObjectProperty}, an empty {@link Option} is returned.
     *
     * @param index The index of the value to retrieve
     * @return The value at the specified index, or an empty {@link Option}
     */
    Option<ObjectProperty> object(int index);

    /**
     * Returns all values in this list as a collection of {@link ObjectProperty} instances.
     *
     * @return All values in this list
     */
    List<ObjectProperty> objects();

    /**
     * Returns the value at the specified index as a {@link ListProperty}. If the value at the specified index is not
     * present, or cannot be represented as a {@link ListProperty}, an empty {@link Option} is returned.
     *
     * @param index The index of the value to retrieve
     * @return The value at the specified index, or an empty {@link Option}
     */
    Option<ListProperty> list(int index);

    /**
     * Returns all values in this list as a collection of {@link ListProperty} instances.
     *
     * @return All values in this list
     */
    List<ListProperty> lists();

    /**
     * Parses the values of this list using the provided {@link ListPropertyParser}. The parser is used to convert the
     * values in this list to a collection of the specified type.
     *
     * @param parser The parser to use to convert the values in this list
     * @param <T> The type of the values in the resulting collection
     *
     * @return A collection of values of the specified type
     */
    <T> Collection<T> parse(ListPropertyParser<T> parser);
}
