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

package org.dockbox.hartshorn.inject.provider.singleton;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.dockbox.hartshorn.inject.ComponentKey;
import org.dockbox.hartshorn.util.IllegalModificationException;
import org.dockbox.hartshorn.util.option.Option;

/**
 * A singleton cache implementation that uses a {@link ConcurrentHashMap} to store
 * instances. This implementation is thread-safe.
 *
 * @see SingletonCache
 * @see ConcurrentHashMap
 *
 * @since 0.4.11
 *
 * @author Guus Lieben
 */
public class ConcurrentHashSingletonCache implements SingletonCache {

    private final Map<ComponentKey<?>, Object> cache = new ConcurrentHashMap<>();
    private final Set<ComponentKey<?>> locked = ConcurrentHashMap.newKeySet();

    @Override
    public void lock(ComponentKey<?> key) {
        if (!this.cache.containsKey(key)) {
            throw new IllegalModificationException("Cannot lock a key that is not present in the cache");
        }
        this.locked.add(key);
    }

    @Override
    public <T> void put(ComponentKey<T> key, T instance) {
        if (this.locked.contains(key) && this.cache.get(key) != instance) {
            throw new IllegalModificationException("Another instance is already stored for key '" + key + "'");
        }
        this.cache.put(key, instance);
    }

    @Override
    public <T> Option<T> get(ComponentKey<T> key) {
        return Option.of(key.type().cast(this.cache.get(key)));
    }

    @Override
    public <T> boolean contains(ComponentKey<T> key) {
        return this.cache.containsKey(key);
    }
}
