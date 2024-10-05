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
import java.util.Comparator;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SequencedSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * A {@link MultiMap} implementation that uses {@link ConcurrentHashMap#newKeySet()} as its backing
 * collection, and a {@link ConcurrentSkipListMap} as its backing map. This implementation is thread-safe.
 *
 * @param <K> the type of the keys
 * @param <V> the type of the values
 *
 * @see ConcurrentHashMap#newKeySet()
 * @see ConcurrentSkipListMap
 * @see MultiMap
 * @see NavigableMultiMap
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public class ConcurrentSetTreeMultiMap<K extends Comparable<K>, V> extends NavigableMultiMap<K, V> {

    public ConcurrentSetTreeMultiMap() {
        this(Comparator.naturalOrder());
    }

    public ConcurrentSetTreeMultiMap(Comparator<? super K> comparator) {
        super(comparator);
    }

    public ConcurrentSetTreeMultiMap(Comparator<? super K> comparator, MultiMap<K, V> map) {
        super(comparator, map);
    }

    @Override
    protected Collection<V> createEmptyCollection() {
        return ConcurrentHashMap.newKeySet();
    }

    @Override
    protected NavigableMap<K, Collection<V>> createEmptyMap() {
        return new ConcurrentSkipListMap<>();
    }

    @Override
    public SequencedSet<K> keySet() {
        return (SequencedSet<K>) super.keySet();
    }
}
