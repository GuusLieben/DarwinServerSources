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
import java.util.Map.Entry;
import java.util.Set;
import org.dockbox.hartshorn.util.CollectionUtilities;

/**
 * A base implementation of a {@link BiMultiMap}. This implementation handles the inverse map
 * creation, but leaves the actual storage and creation of the inverse map to the implementation.
 *
 * @param <K> the type of the keys
 * @param <V> the type of the values
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public abstract class AbstractBiMultiMap<K, V> extends StandardMultiMap<K, V> implements BiMultiMap<K, V> {

    protected AbstractBiMultiMap() {
    }

    protected AbstractBiMultiMap(MultiMap<K, V> map) {
        super(map);
    }

    @Override
    public MultiMap<V, K> inverse() {
        Set<Entry<K, Collection<V>>> entries = this.entrySet();
        MultiMap<V, K> inverseMap = this.createEmptyInverseMap();
        CollectionUtilities.iterateEntries(entries, (key, values) -> {
            for (V value : values) {
                inverseMap.put(value, key);
            }
        });
        return inverseMap;
    }

    /**
     * Creates an empty inverse map. This method is invoked by {@link #inverse()}. The returned map
     * is populated with the inverse mappings of the current instance, and thus expected to be empty.
     *
     * @return An empty inverse map.
     */
    protected abstract MultiMap<V, K> createEmptyInverseMap();
}
