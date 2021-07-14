/*
 * Copyright (C) 2020 Guus Lieben
 *
 * This framework is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.hartshorn.persistence.registry;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.entity.annotations.Entity;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings({ "UnusedReturnValue", "unused" })
@Entity(value = "column")
public class RegistryColumn<T> extends ArrayList<T> {

    public RegistryColumn() {
        super();
    }

    public RegistryColumn(Collection<T> values) {
        this.addAll(HartshornUtils.asList(values));
    }

    /**
     * Filters the RegistryColumn based on the provided filter.
     *
     * @param filter
     *         The filter accepts a value of type {@code T} or its parents and returns false to
     *         keep that value, true to remove it.
     *
     * @return Itself
     */
    public RegistryColumn<T> removeValueIf(Predicate<? super T> filter) {
        this.removeIf(filter);
        return this;
    }

    /**
     * Maps this registryColumn to another type.
     *
     * @param mapper
     *         The mapper accepts a value of type {@code T} or its parents and returns a value
     *         of type {@code K}.
     * @param <K>
     *         The type of the new RegistryColumn.
     *
     * @return A new RegistryColumn which contains the mapped values of the previous RegistryColumn.
     */
    public <K> RegistryColumn<K> map(Function<? super T, K> mapper) {
        RegistryColumn<K> result = new RegistryColumn<>();

        for (T value : this) {
            result.add(mapper.apply(value));
        }
        return result;
    }

    /**
     * Maps this registryColumn to a collection and then adds all the collections into a new single
     * RegistryColumn
     *
     * @param mapper
     *         The mapper accepts a value of type {@code T} or its parents and returns a
     *         collection of type {@code K}.
     * @param <K>
     *         The type of the new RegistryColumn.
     *
     * @return A new RegistryColumn which contains all the values of the collections.
     */
    public <K> RegistryColumn<K> flatMap(Function<? super T, ? extends Collection<K>> mapper) {
        RegistryColumn<K> result = new RegistryColumn<>();

        for (T value : this) {
            result.addAll(mapper.apply(value));
        }
        return result;
    }

    /**
     * Attempts to cast the values to the specified type {@code K}. If the value is not an instance of
     * type {@code K} then it is not added to the resulting RegistryColumn.
     *
     * @param clazz
     *         The class of the type to convert to.
     * @param <K>
     *         The type of the new RegistryColumn
     *
     * @return A new RegistryColumn which contains all the values of the previous RegistryColumn that
     *         could be converted.
     */
    public <K extends T> RegistryColumn<K> convertTo(Class<K> clazz) {
        RegistryColumn<K> result = new RegistryColumn<>();

        for (T value : this) {
            if (clazz.isInstance(value)) {
                @SuppressWarnings("unchecked")
                K convertedValue = (K) value;
                result.add(convertedValue);
            }
        }
        return result;
    }

    /**
     * Finds the first value which matches the provided predicate.
     *
     * @param predicate
     *         The predicate takes in a value of type {@code T} or its parents and returns
     *         true if that value is a match, otherwise it returns false.
     *
     * @return An {@link Exceptional} containing the value of the first match, if one is found.
     */
    public Exceptional<T> firstMatch(Predicate<? super T> predicate) {
        for (T value : this) {
            if (predicate.test(value)) return Exceptional.of(value);
        }
        return Exceptional.empty();
    }

    /**
     * Safely returns the first element in the RegistryColumn.
     *
     * @return An {@link Exceptional} containing the first element in the RegistryColumn, if one is
     *         found.
     */
    public Exceptional<T> first() {
        return this.safe(0);
    }

    /**
     * Safely get an element by wrapping it within an {@link Exceptional}.
     *
     * @param index
     *         The index of the element to retrieve.
     *
     * @return An {@link Exceptional} containing the element at the provided index in the
     *         RegistryColumn, if one is found.
     */
    public Exceptional<T> safe(int index) {
        return Exceptional.of(() -> this.get(index));
    }
}
