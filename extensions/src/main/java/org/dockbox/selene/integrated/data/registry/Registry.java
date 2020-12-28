/*
 *  Copyright (C) 2020 Guus Lieben
 *
 *  This framework is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 2.1 of the
 *  License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 *  the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.selene.integrated.data.registry;

import org.dockbox.selene.core.SeleneUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Registry<V> {

    private final Map<RegistryIdentifier, RegistryColumn<V>> data = SeleneUtils.emptyConcurrentMap();

    /**
     * Adds a column of data to the Registry. <B>Note</B> this will override an existing column
     * if they share the same {@link RegistryIdentifier}
     *
     * @param columnID
     *         The {@link RegistryIdentifier} for which to add this data added under.
     * @param values
     *         A safe varargs of type {@link V} to be added.
     *
     * @return Itself.
     */
    @SafeVarargs
    public final Registry<V> addColumn(RegistryIdentifier columnID, V... values) {
        return this.addColumn(columnID, Arrays.asList(values));
    }

    /**
     * Adds a column of data to the Registry. <B>Note</B> this will override an existing column
     * if they share the same {@link RegistryIdentifier}
     *
     * @param columnID
     *         The {@link RegistryIdentifier} for which to add this data added under.
     * @param values
     *         A collection of type {@link V} or its children to be added.
     *
     * @return Itself.
     */
    public Registry<V> addColumn(RegistryIdentifier columnID, Collection<? extends V> values) {
        this.data.put(columnID, new RegistryColumn<>(values));
        return this;
    }

    /**
     * Adds another Registry to this one. If the added Registry contains the same {@link RegistryIdentifier}s, then that
     * data will be added to the existing columns. The difference between this method and {@link #addRegistry(Registry)}
     * is that this allows the direct insertion of a {@link Map} instance instead of requiring a pre-existing registry.
     *
     * @param otherRegistry
     *         The other Registry to add to this one.
     *
     * @return Itself.
     */
    public Registry<V> addRegistry(@NotNull Map<RegistryIdentifier, RegistryColumn<V>> otherRegistry) {
        otherRegistry.forEach(this::addData);
        return this;
    }

    /**
     * Adds another Registry to this one. If the added Registry contains the same {@link RegistryIdentifier}s, then that
     * data will be added to the existing columns.
     *
     * @param otherRegistry
     *         The other Registry to add to this one.
     *
     * @return Itself.
     */
    public Registry<V> addRegistry(@NotNull Registry<V> otherRegistry) {
        return this.addRegistry(otherRegistry.data);
    }

    /**
     * Adds data to the Registry. If the columnID does not exist, it creates a new column, otherwise it
     * adds the data to the existing column.
     *
     * @param columnID
     *         The {@link RegistryIdentifier} for which this data will be added to.
     * @param values
     *         A collection of type {@link V} or its children to be added.
     *
     * @return Itself.
     */
    public Registry<V> addData(RegistryIdentifier columnID, Collection<? extends V> values) {
        if (this.data.containsKey(columnID)) {
            this.data.get(columnID).addAll(values);
        } else {
            this.addColumn(columnID, values);
        }
        return this;
    }

    /**
     * @param columnIDs
     *         A varargs of {@link RegistryIdentifier}s to remove from the Registry if contained.
     *
     * @return Itself.
     */
    public Registry<V> removeColumns(@NotNull RegistryIdentifier... columnIDs) {
        for (RegistryIdentifier columnID : columnIDs) {
            this.data.remove(columnID);
        }
        return this;
    }

    /**
     * @param columnIDs
     * A varargs of {@link RegistryIdentifier}s to check if contained in the Registry.
     *
     * @return True if all of the {@link RegistryIdentifier}s are contained, otherwise false.
     */
    public boolean containsColumns(RegistryIdentifier... columnIDs) {
        for (RegistryIdentifier columnID : columnIDs) {
            if (!this.data.containsKey(columnID)) return false;
        }
        return true;
    }

    /**
     * Gets all the matching columns in the Registry if contained.
     *
     * @param columnIDs
     * A varargs of {@link RegistryIdentifier}s to return from the Registry if contained.
     *
     * @return
     * All the matching columns data combined into a single {@link RegistryColumn}. If no matches are found, an empty
     * {@link RegistryColumn} will be returned.
     */
    public RegistryColumn<V> getMatchingColumns(RegistryIdentifier... columnIDs) {
        RegistryColumn<V> result = new RegistryColumn<>();
        for (RegistryIdentifier columnID : columnIDs) {
            if (this.data.containsKey(columnID)) {
                result.addAll(this.data.get(columnID));
            }
        }
        return result;
    }

    /**
     * Filter the Registry by its columns. Note this creates a new Registry and doesn't modify itself.
     *
     * @param filter
     * The filter accepts a {@link RegistryIdentifier} and returns true to remove that column, false to keep it.
     * The columns which pass the filter are stored in a <b>new</b> Registry.
     *
     * @return The new Registry containing the filtered columns.
     */
    public Registry<V> removeColumnsIf(Predicate<RegistryIdentifier> filter) {
        Registry<V> registry = new Registry<>();

        for (RegistryIdentifier columnID : this.data.keySet()) {
            if (!filter.test(columnID)) {
                registry.addColumn(columnID, this.data.get(columnID));
            }
        }
        return registry;
    }

    /**
     * @return All the data in the Registry combined into a single {@link RegistryColumn}
     */
    public RegistryColumn<V> getAllData() {
        RegistryColumn<V> result = new RegistryColumn<>();
        for (RegistryColumn<V> columnData : this.data.values()) {
            result.addAll(columnData);
        }
        return result;
    }

    /**
     * Filter the Registry by its columns. Note this creates a new Registry and doesn't modify itself.
     *
     * @param biFilter
     *         The biFilter accepts a {@link RegistryIdentifier}, along with its {@link RegistryColumn} and returns true to
     *         remove that column, false to keep it. The columns which pass the filter are stored in a <b>new</b> Registry.
     *
     * @return The new Registry containing the filtered columns.
     */
    public Registry<V> removeColumnsIf(BiPredicate<RegistryIdentifier, RegistryColumn<? super V>> biFilter) {
        Registry<V> registry = new Registry<>();

        this.data.forEach((columnID, column) -> {
            if (!biFilter.test(columnID, column)) {
                registry.addColumn(columnID, column);
            }
        });
        return registry;
    }

    /**
     * Filter the Registry by its values. Note this creates a new Registry and doesn't modify itself.
     *
     * @param filter
     * The filter accepts a value of type {@link V} or its parents and returns true to remove that column, false to keep it.
     * The values which pass the filter are stored in a <b>new</b> Registry. If no values in a particular column pass the
     * filter, it is still added to the new Registry, it will simply contain no values.
     *
     * @return The new Registry containing the filtered values.
     */
    public Registry<V> removeValuesIf(Predicate<? super V> filter) {
        Registry<V> registry = new Registry<>();

        for (RegistryIdentifier columnID : this.data.keySet()) {
            RegistryColumn<V> column = new RegistryColumn<>(this.data.get(columnID));
            column.removeValueIf(filter);
            registry.addColumn(columnID, column);
        }
        return registry;
    }

    /**
     * Filter the Registry by its values. Note this creates a new Registry and doesn't modify itself.
     *
     * @param biFilter
     * The biFilter accepts a {@link RegistryIdentifier} (The columnID of the value), along with a value of type {@link V}
     * or its parents and returns true to remove that column, false to keep it. The values which pass the filter are stored
     * in a <b>new</b> Registry. If no values in a particular column pass the filter, it is still added to the new Registry,
     * it will simply contain no values.
     *
     * @return The new Registry containing the filtered values.
     */
    public Registry<V> removeValuesIf(BiPredicate<RegistryIdentifier, ? super V> biFilter) {
        Registry<V> registry = new Registry<>();

        this.data.forEach((columnID, column) -> column.forEach(v -> {
            if (!biFilter.test(columnID, v)) {
                registry.addData(columnID, v);
            }
        }));
        return registry;
    }

    /**
     * Adds data to the Registry. If the columnID does not exist, it creates a new column, otherwise it
     * adds the data to the existing column.
     *
     * @param columnID
     *         The {@link RegistryIdentifier} for which this data will be added to.
     * @param values
     *         A safe varargs of type {@link V} to be added.
     *
     * @return Itself.
     */
    @SafeVarargs
    public final Registry<V> addData(RegistryIdentifier columnID, V... values) {
        return this.addData(columnID, Arrays.asList(values));
    }

    /**
     * Delegated from {@link Map#get(Object)}
     *
     * @param key
     *         The key whose associated value is to be returned
     *
     * @return The value to which the specified key is mapped, or
     *         {@code null} if this map contains no mapping for the key
     */
    public RegistryColumn<V> get(Object key) {
        return this.data.get(key);
    }

    /**
     * Delegated from {@link Map#size()}
     *
     * @return The number of key-value mappings in this map
     */
    public int size() {
        return this.data.size();
    }

    /**
     * Delegated from {@link Map#isEmpty()}
     *
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    /**
     * Delegated from {@link Map#getOrDefault(Object, Object)}
     *
     * @param key
     *         The key whose associated value is to be returned
     * @param defaultValue
     *         The default mapping of the key
     *
     * @return The value to which the specified key is mapped, or
     */
    public RegistryColumn<V> getOrDefault(Object key, RegistryColumn<V> defaultValue) {
        return this.data.getOrDefault(key, defaultValue);
    }

    /**
     * Delegated from {@link Map#forEach(BiConsumer)}
     *
     * @param action
     *         The action to be performed for each entry
     */
    public void forEach(BiConsumer<? super RegistryIdentifier, ? super RegistryColumn<V>> action) {
        this.data.forEach(action);
    }
}
