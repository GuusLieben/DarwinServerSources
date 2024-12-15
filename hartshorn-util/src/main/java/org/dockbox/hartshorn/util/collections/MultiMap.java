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

package org.dockbox.hartshorn.util.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * A map that can contain multiple values for a single key. This allows for a more natural way of storing multiple values
 * for a single key, without having to use a {@link Map} of {@link Collection}s.
 *
 * @param <K> the type of the keys
 * @param <V> the type of the values
 *
 * @since 0.4.12
 *
 * @author Guus Lieben
 */
public interface MultiMap<K, V> {

    /**
     * Returns all values in this {@link MultiMap}. The returned {@link Collection} is a copy of the values in this
     * {@link MultiMap}, so changes to the returned {@link Collection} will not be reflected in this {@link MultiMap}.
     *
     * <p>The returned {@link Collection} is not guaranteed to be ordered in any way, and may contain duplicate values.
     *
     * @return all values in this {@link MultiMap}
     */
    Collection<V> allValues();

    /**
     * Adds all values in the given {@link Collection} to the given key. If the key already exists in this {@link
     * MultiMap}, the values in the given {@link Collection} are added to the existing values. If the key does not
     * exist in this {@link MultiMap}, the key is added to this {@link MultiMap} and the values in the given {@link
     * Collection} are added to the newly created entry.
     *
     * @param key the key to add the values to
     * @param values the values to add
     */
    void putAll(K key, Collection<V> values);

    /**
     * Adds all entries in the given {@link MultiMap} to this {@link MultiMap}. If a key already exists in this {@link
     * MultiMap}, the values in the given {@link MultiMap} are added to the existing values. If a key does not exist
     * in this {@link MultiMap}, the key is added to this {@link MultiMap} and the values in the given {@link
     * MultiMap} are added to the newly created entry.
     *
     * @param map the {@link MultiMap} to add
     */
    void putAll(MultiMap<K, V> map);

    /**
     * Adds the given value to the given key. If the key already exists in this {@link MultiMap}, the value is added
     * to the existing values. If the key does not exist in this {@link MultiMap}, the key is added to this {@link
     * MultiMap} and the value is added to the newly created entry.
     *
     * @param key the key to add the value to
     * @param value the value to add
     */
    void put(K key, V value);

    /**
     * Adds the given value to the given key, if the key does not already exist in this {@link MultiMap}. If the key
     * already exists in this {@link MultiMap}, the value is not added.
     *
     * @param key the key to add the value to
     * @param value the value to add
     */
    void putIfAbsent(K key, V value);

    /**
     * Returns the values associated with the given key, or an empty {@link Collection} if the key does not exist in
     * this {@link MultiMap}.
     *
     * @param key the key
     * @return the values associated with the given key, or an empty {@link Collection} if the key does not exist
     */
    Collection<V> get(K key);

    /**
     * Returns the set of keys in this {@link MultiMap}. The returned {@link Set} is a copy of the keys in this
     * {@link MultiMap}, so changes to the returned {@link Set} will not be reflected in this {@link MultiMap}.
     *
     * @return the set of keys in this {@link MultiMap}
     */
    Set<K> keySet();

    /**
     * Returns the set of entries in this {@link MultiMap}. The returned {@link Set} is a copy of the entries in this
     * {@link MultiMap}, so changes to the returned {@link Set} will not be reflected in this {@link MultiMap}.
     *
     * @return the set of entries in this {@link MultiMap}
     */
    Set<Map.Entry<K, Collection<V>>> entrySet();

    /**
     * Returns the values in this {@link MultiMap}. The returned {@link Collection} is a copy of the values in this
     * {@link MultiMap}, so changes to the returned {@link Collection} will not be reflected in this {@link
     * MultiMap}.
     *
     * @return the values in this {@link MultiMap}
     */
    Collection<Collection<V>> values();

    /**
     * Returns whether this {@link MultiMap} contains the given key.
     *
     * @param key the key
     * @return whether this {@link MultiMap} contains the given key
     */
    boolean containsKey(K key);

    /**
     * Returns whether this {@link MultiMap} contains the given value.
     *
     * @param value the value
     * @return whether this {@link MultiMap} contains the given value
     */
    boolean containsValue(V value);

    /**
     * Returns whether this {@link MultiMap} contains the given entry.
     *
     * @param key the key
     * @param value the value
     * @return whether this {@link MultiMap} contains the given entry
     */
    boolean containsEntry(K key, V value);

    /**
     * Removes all values associated with the given key from this {@link MultiMap} and returns the removed values. If
     * the key does not exist in this {@link MultiMap}, an empty {@link Collection} is returned.
     *
     * @param key the key
     * @return the removed values
     */
    Collection<V> remove(K key);

    /**
     * Removes all entries from this {@link MultiMap}.
     */
    void clear();

    /**
     * Returns the number of values in this {@link MultiMap}. If a key is associated with multiple values, each value
     * is counted separately.
     *
     * @return the number of values in this {@link MultiMap}
     */
    int size();

    /**
     * Returns whether this {@link MultiMap} is empty. If a key is in the map, but has no values associated with it,
     * the key is still counted as being in the map.
     *
     * @return whether this {@link MultiMap} is empty
     */
    boolean isEmpty();

    /**
     * Removes the given value from the given key. If the key is in this {@link MultiMap} but the value is not, this
     * method does nothing.
     *
     * @param key the key
     * @param value the value
     * @return whether the value was removed
     */
    boolean remove(K key, V value);

    /**
     * Removes all values associated with the given value from this {@link MultiMap}.
     *
     * @param processor the value to remove
     * @return the number of values removed
     */
    int removeValue(V processor);

    /**
     * Replaces the given old value with the given new value in the given key. If the key is in this {@link
     * MultiMap} but the old value is not, this method does nothing.
     *
     * @param key the key
     * @param oldValue the old value
     * @param newValue the new value
     * @return whether the value was replaced
     */
    boolean replace(K key, V oldValue, V newValue);

    /**
     * Iterates over all entries in this {@link MultiMap} and calls the given {@link BiConsumer} for each entry.
     *
     * @param consumer the {@link BiConsumer}
     */
    void forEach(BiConsumer<K, V> consumer);

    /**
     * Creates a new {@link MultiMapBuilder} which can be used to create a new {@link MultiMap} with a custom
     * implementation.
     *
     * @param <K> the type of the keys
     * @param <V> the type of the values
     *
     * @return a new {@link MultiMapBuilder}
     */
    static <K, V> MultiMapBuilder<K, V> builder() {
        return new MultiMapBuilder<>();
    }
}
