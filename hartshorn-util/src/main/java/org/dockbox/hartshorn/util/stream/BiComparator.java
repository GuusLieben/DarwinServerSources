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

package org.dockbox.hartshorn.util.stream;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A functional interface for comparing two values of different types. This serves as a utility for
 * comparing {@link Map.Entry} instances, typically through {@link EntryStream} instances.
 *
 * @param <K> the key type
 * @param <V> the value type
 *
 * @see Comparator
 * @see Map.Entry
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
@FunctionalInterface
public interface BiComparator<K, V> extends Comparator<Map.Entry<K, V>> {

    @Override
    default int compare(Entry<K, V> kvEntry, Entry<K, V> t1) {
        return this.compare(kvEntry.getKey(), kvEntry.getValue(), t1.getKey(), t1.getValue());
    }

    int compare(K key1, V value1, K key2, V value2);
}
