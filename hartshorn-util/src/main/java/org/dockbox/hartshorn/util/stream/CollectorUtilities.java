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

import org.dockbox.hartshorn.util.collections.ArrayListMultiMap;
import org.dockbox.hartshorn.util.collections.MultiMap;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;

/**
 * A utility class for custom collectors which target custom collection types.
 *
 * @see Collector
 * @see MultiMap
 * @see EntryStream
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public final class CollectorUtilities {

    /**
     * A collector that collects entries into a {@link MultiMap}. The default implementation is an
     * {@link ArrayListMultiMap}.
     *
     * @param <K> the key type
     * @param <V> the value type
     *
     * @return a collector that collects entries into a {@link MultiMap}
     */
    public static <K, V> Collector<Entry<K, V>, ?, MultiMap<K, V>> toMultiMap() {
        return toMultiMap(ArrayListMultiMap::new, Entry::getKey, Entry::getValue);
    }

    /**
     * A collector that collects entries into a {@link MultiMap}. The default implementation is an
     * {@link ArrayListMultiMap}.
     *
     * @param keyMapper the function to map the key
     * @param valueMapper the function to map the value
     *
     * @param <T> the type of the stream
     * @param <K> the key type
     * @param <V> the value type
     *
     * @return a collector that collects entries into a {@link MultiMap}
     */
    public static <T, K, V> Collector<T, ?, MultiMap<K, V>> toMultiMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper
    ) {
        return toMultiMap(ArrayListMultiMap::new, keyMapper, valueMapper);
    }

    /**
     * A collector that collects entries into a {@link MultiMap}. The given supplier is used to create the map.
     *
     * @param supplier the supplier to create the map
     * @param keyMapper the function to map the key
     * @param valueMapper the function to map the value
     *
     * @param <T> the type of the stream
     * @param <K> the key type
     * @param <V> the value type
     * @param <M> the type of the map
     *
     * @return a collector that collects entries into a {@link MultiMap}
     */
    public static <T, K, V, M extends MultiMap<K, V>> Collector<T, ?, M> toMultiMap(
            Supplier<M> supplier,
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper
    ) {
        return new RecordCollector<>(
                supplier,
                (map, next) -> map.put(keyMapper.apply(next), valueMapper.apply(next)),
                (left, right) -> {
                    left.putAll(right);
                    return left;
                });
    }

    /**
     * A collector that collects {@link Map.Entry entries} into an {@link EntryStream}.
     *
     * @param <K> the key type
     * @param <V> the value type
     *
     * @return a collector that collects entries into an {@link EntryStream}
     */
    public static <K, V> Collector<Entry<K, V>, ?, EntryStream<K, V>> toEntryStream() {
        return toEntryStream(HashMap::new, Entry::getKey, Entry::getValue);
    }

    /**
     * A collector that collects entries into an {@link EntryStream}.
     *
     * @param keyMapper the function to map the key
     * @param valueMapper the function to map the value
     *
     * @param <T> the type of the stream
     * @param <K> the key type
     * @param <V> the value type
     *
     * @return a collector that collects entries into an {@link EntryStream}
     */
    public static <T, K, V> Collector<T, ?, EntryStream<K, V>> toEntryStream(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper
    ) {
        return toEntryStream(HashMap::new, keyMapper, valueMapper);
    }

    /**
     * A collector that collects entries into an {@link EntryStream}. The given supplier is used to create the map
     * that is used to collect the entries in intermediate steps. The map is discarded once the collector is finished.
     *
     * @param mapSupplier the supplier to create the map
     * @param keyMapper the function to map the key
     * @param valueMapper the function to map the value
     *
     * @param <T> the type of the stream
     * @param <K> the key type
     * @param <V> the value type
     *
     * @return a collector that collects entries into an {@link EntryStream}
     */
    public static <T, K, V> Collector<T, ?, EntryStream<K, V>> toEntryStream(
            Supplier<Map<K, V>> mapSupplier,
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper
    ) {
        return new RecordCollector<>(
                mapSupplier,
                (map, next) -> map.put(keyMapper.apply(next), valueMapper.apply(next)),
                (left, right) -> {
                    left.putAll(right);
                    return left;
                },
                EntryStream::of,
                EnumSet.of(Characteristics.IDENTITY_FINISH)
        );
    }

    /**
     * Simple record implementation of a {@link Collector}.
     *
     * @param supplier the supplier to create the accumulator
     * @param accumulator the function to accumulate the elements
     * @param combiner the function to combine the accumulators
     * @param finisher the function to finish the collection
     * @param characteristics the characteristics of the collector
     * @param <T> the type of the stream
     * @param <A> the type of the accumulator
     * @param <R> the type of the result
     *
     * @see Collector
     *
     * @since 0.7.0
     *
     * @author Guus Lieben
     */
    public record RecordCollector<T, A, R>(
            Supplier<A> supplier,
            BiConsumer<A, T> accumulator,
            BinaryOperator<A> combiner,
            Function<A, R> finisher,
            Set<Characteristics> characteristics
    ) implements Collector<T, A, R> {

        RecordCollector(
                Supplier<A> supplier,
                BiConsumer<A, T> accumulator,
                BinaryOperator<A> combiner
        ) {
            this(
                    supplier,
                    accumulator,
                    combiner,
                    a -> (R) a,
                    EnumSet.of(Characteristics.IDENTITY_FINISH)
            );
        }
    }
}
