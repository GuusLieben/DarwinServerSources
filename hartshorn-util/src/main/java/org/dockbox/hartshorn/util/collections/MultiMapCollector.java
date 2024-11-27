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

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A {@link Collector} that collects elements into a {@link MultiMap}. This collector does not ensure that the
 * resulting values of the {@link MultiMap} are ordered, unless the {@link MultiMap} implementation itself does.
 * Similarly, the resulting {@link MultiMap} may not be thread-safe, unless the {@link MultiMap} implementation
 * itself is.
 *
 * <p>All resulting {@link MultiMap} instances are wrapped in a {@link UnmodifiableMultiMap}, even if a custom
 * {@link MultiMap} implementation is used.
 *
 * @param supplier A supplier that creates a new {@link MultiMap} instance
 * @param keyMapper A function that maps elements to keys
 * @param valueMapper A function that maps elements to values
 * @param <T> The type of the elements to collect
 * @param <K> The type of the keys in the resulting {@link MultiMap}
 * @param <V> The type of the values in the resulting {@link MultiMap}
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public record MultiMapCollector<T, K, V>(
    Supplier<MultiMap<K, V>> supplier,
    Function<T, K> keyMapper,
    Function<T, V> valueMapper
) implements Collector<T, MultiMap<K, V>, MultiMap<K, V>> {

    public static <T, K> MultiMapCollector<T, K, T> toMultiMap(Function<T, K> keyMapper) {
        return toMultiMap(keyMapper, Function.identity());
    }

    public static <T, K, V> MultiMapCollector<T, K, V> toMultiMap(Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return toMultiMap(ArrayListHashBiMultiMap::new, keyMapper, valueMapper);
    }

    public static <T, K, V> MultiMapCollector<T, K, V> toMultiMap(Supplier<MultiMap<K, V>> mapSupplier, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return new MultiMapCollector<>(mapSupplier, keyMapper, valueMapper);
    }

    @Override
    public BiConsumer<MultiMap<K, V>, T> accumulator() {
        return (map, element) -> map.put(this.keyMapper().apply(element), this.valueMapper().apply(element));
    }

    @Override
    public BinaryOperator<MultiMap<K, V>> combiner() {
        return (left, right) -> {
            left.putAll(right);
            return left;
        };
    }

    @Override
    public Function<MultiMap<K, V>, MultiMap<K, V>> finisher() {
        return UnmodifiableMultiMap::new;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED, Characteristics.CONCURRENT);
    }
}
