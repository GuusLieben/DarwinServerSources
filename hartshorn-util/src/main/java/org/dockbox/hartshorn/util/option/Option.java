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

package org.dockbox.hartshorn.util.option;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.transform.Result;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.dockbox.hartshorn.context.Context;
import org.dockbox.hartshorn.util.TypeUtils;
import org.dockbox.hartshorn.util.option.none.None;
import org.dockbox.hartshorn.util.option.some.Some;

/**
 * A container object which may or may not contain a non-null value. If a value is present, {@link #present()} will
 * return {@code true} and {@link #get()} will return the value. If no value is present, {@link #absent()} will return
 * {@code true}.
 *
 * <p>Additional methods that depend on the presence or absence of a contained value are provided,
 * such as {@link #orElse(Object)} (return a default value if value not present) and {@link #peek(Consumer)} (execute a
 * block of code if the value is present).
 *
 * <p>When comparing {@link Option} to {@link Optional}, there are two major differences between the two:
 * <ol>
 *     <li>
 *         {@link Option} is an interface, with specific implementations to match certain scenarios (present or absent values).
 *         This allows for pattern matching, and for the creation of custom implementations if desired.
 *     </li>
 *     <li>
 *         {@link Option} is a {@link Context}, which allows for the storage of additional data. This is useful for example when
 *         a value may be present, but the value is not the only thing that is relevant. For example, when a value is absent, and
 *         the reason for the absence is relevant, the reason can be stored in the {@link Context} of the {@link Option} instance.
 *     </li>
 * </ol>
 *
 * @param <T> the type of the (potential) value wrapped by the {@link Option} instance.
 *
 * @since 0.4.13
 *
 * @author Guus Lieben
 */
public interface Option<T> extends Context, Iterable<T> {

    /**
     * Creates a new {@link Option} instance wrapping the given nullable value. If the value is {@code null}, an
     * {@link Option} instance representing an absent value is returned.
     *
     * @param value the value to wrap.
     * @param <T> the type of the (potential) value wrapped by the {@link Option} instance.
     *
     * @return a new {@link Option} instance wrapping the given nullable value.
     */
    @NonNull
    static <T> Option<T> of(@Nullable T value) {
        if (value == null) {
            return new None<>();
        }
        return new Some<>(value);
    }

    /**
     * Creates a new {@link Option} instance based on the given {@link Optional} instance. If the {@link Optional}
     * instance is empty, an {@link Option} instance representing an absent value is returned. If the {@link Optional}
     * instance is present, an {@link Option} instance representing the present value is returned.
     *
     * @param optional the {@link Optional} instance to wrap.
     * @param <T> the type of the (potential) value wrapped by the {@link Option} instance.
     *
     * @return a new {@link Option} instance wrapping the (potential) value of the given {@link Optional} instance.
     */
    @NonNull
    static <T> Option<T> of(@NonNull Optional<@Nullable T> optional) {
        if (optional.isEmpty()) {
            return new None<>();
        }
        return new Some<>(optional.get());
    }

    /**
     * Creates a {@link Result} instance based on the provided {@link Callable}. If the callable throws any kind of
     * exception, an {@link Option} instance representing an absent value is returned. If the callable returns a
     * non-null value, an {@link Option} instance representing the present value is returned. If the callable returns
     * {@code null}, an {@link Option} instance representing an absent value is returned.
     *
     * @param supplier the {@link Callable} to execute.
     * @param <T> the type of the (potential) value wrapped by the {@link Option} instance.
     *
     * @return a new {@link Option} instance wrapping the (potential) value of the given {@link Callable} instance.
     */
    @NonNull
    static <T> Option<T> of(@NonNull Callable<@Nullable T> supplier) {
        try {
            T value = supplier.call();
            if (value == null) {
                return new None<>();
            }
            return new Some<>(value);
        }
        catch (Exception e) {
            return new None<>();
        }
    }

    /**
     * Creates a new {@link Option} instance which contains no value.
     *
     * @param <T> the type of the (potential) value wrapped by the {@link Option} instance.
     *
     * @return a new {@link Option} instance which contains no value.
     */
    @NonNull
    static <T> Option<T> empty() {
        return new None<>();
    }

    /**
     * Returns a {@link Attempt} instance based on the current {@link Option} instance. If the current instance
     * is already a {@link Attempt}, it is returned. If the current instance is an {@link Option}, a new
     * {@link Attempt} instance is created.
     *
     * @param errorType the type of the error to wrap in the {@link Attempt} instance.
     * @param <E> the type of the error to wrap in the {@link Attempt} instance.
     *
     * @return a {@link Attempt} instance based on the current {@link Option} instance.
     *
     * @deprecated since 0.6.0 for removal in 0.7.0. Consider handling exceptions immediately, or rethrowing them
     *             in a checked manner.
     */
    @Deprecated(since = "0.6.0", forRemoval = true)
    <E extends Throwable> Attempt<T, E> attempt(Class<E> errorType);

    /**
     * If a value is present in this {@link Option}, the given {@link Consumer} is executed with the value as argument.
     * If no value is present, the given {@link Consumer} is not executed.
     *
     * @param consumer the {@link Consumer} to execute.
     *
     * @return the current {@link Option} instance.
     */
    @NonNull
    Option<T> peek(Consumer<T> consumer);

    /**
     * If no value is present in this {@link Option}, the given {@link Runnable} is executed. If a value is present, the
     * given {@link Runnable} is not executed.
     *
     * @param runnable the {@link Runnable} to execute.
     *
     * @return the current {@link Option} instance.
     */
    @NonNull
    Option<T> onEmpty(Runnable runnable);

    /**
     * If a value is present in this {@link Option}, the value is returned. If no value is present, a
     * {@link NoSuchElementException} is thrown. Before this method is called, it is recommended to check if a value is
     * present using {@link #present()}.
     *
     * @return the value wrapped by the {@link Option} instance.
     * @throws NoSuchElementException if no value is present.
     */
    @NonNull
    T get();

    /**
     * If a value is present in this {@link Option}, the value is returned. If no value is present, {@code null} is
     * returned.
     *
     * @return the value wrapped by the {@link Option} instance, or {@code null} if no value is present.
     */
    @Nullable
    T orNull();

    /**
     * If a value is present in this {@link Option}, the value is returned. If no value is present, the given default
     * value is returned.
     *
     * @param value the default value to return if no value is present.
     *
     * @return the value wrapped by the {@link Option} instance, or the given default value if no value is present.
     */
    @Nullable
    T orElse(@Nullable T value);

    /**
     * Equivalent of {@link #orCompute(Supplier)}, but always returns the result of either the current {@link Option},
     * or the given {@link Supplier}. If the current {@link Option} is empty, and the result of the supplier is {@code
     * null}, then {@code null} is returned.
     *
     * @param supplier the {@link Supplier} to execute if no value is present.
     *
     * @return the value wrapped by the {@link Option} instance, or the value returned by the given supplier.
     */
    @Nullable
    T orElseGet(@NonNull Supplier<@Nullable T> supplier);

    /**
     * If a value is present in this {@link Option}, the value is returned. If no value is present, the error returned
     * by the given {@link Supplier} is thrown. The {@link Supplier} is only executed if no value is present.
     *
     * @param supplier the {@link Supplier} to execute if no value is present.
     * @param <E> the type of the error to throw.
     *
     * @return the value wrapped by the {@link Option} instance.
     * @throws E if no value is present.
     */
    @NonNull <E extends Throwable> T orElseThrow(@NonNull Supplier<@NonNull E> supplier) throws E;

    /**
     * If no value is present in this {@link Option}, the given {@link Supplier} is executed. The result of the
     * {@link Supplier} is then returned wrapped in a new {@link Option} instance. If a value is present, the current
     * {@link Option} instance is returned.
     *
     * @param supplier the {@link Supplier} to execute if no value is present.
     *
     * @return a new {@link Option} instance wrapping the result of the given {@link Supplier}, or the current
     *         {@link Option} instance if a value is present.
     */
    @NonNull
    Option<T> orCompute(@NonNull Supplier<@Nullable T> supplier);

    /**
     * If no value is present in this {@link Option}, the given {@link Supplier} is executed. The {@link Option}
     * returned by the {@link Supplier} is then returned. If a value is present, the current {@link Option} instance is
     * returned.
     *
     * @param supplier the {@link Supplier} to execute if no value is present.
     *
     * @return the {@link Option} returned by the given {@link Supplier}, or the current {@link Option} instance if a
     *         value is present.
     */
    @NonNull
    Option<T> orComputeFlat(@NonNull Supplier<@NonNull Option<T>> supplier);

    /**
     * Return {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if there is a value present, otherwise {@code false}
     */
    boolean present();

    /**
     * Return {@code true} if there is no value present, otherwise {@code false}. Acts as an inverse of
     * {@link Option#present()}.
     *
     * @return {@code true} if there is no value present, otherwise {@code false}
     */
    boolean absent();

    /**
     * Returns a new {@link Optional} instance wrapping the value wrapped by the current {@link Option} instance. If no
     * value is present, an empty {@link Optional} is returned.
     *
     * @return a new {@link Optional} instance wrapping the value wrapped by the current {@link Option} instance, or an
     *         empty {@link Optional} if no value is present.
     */
    @NonNull
    Optional<T> optional();

    /**
     * Transforms the value wrapped by the current {@link Option} instance using the given {@link Function}. If the
     * current {@link Option} instance does not contain a value, an empty {@link Option} is returned. If the given
     * {@link Function} returns {@code null}, no exception is thrown, and an empty {@link Option} is returned.
     *
     * @param <U> The type of the result of the mapping function
     * @param function A mapping function to apply to the value, if present
     *
     * @return an {@link Option} describing the result of applying a mapping function to the value of this
     *         {@link Option}, if a value is present, otherwise {@link Option#empty()}
     */
    @NonNull <U> Option<U> map(@NonNull Function<@NonNull T, @Nullable U> function);

    /**
     * Transforms the value wrapped by the current {@link Option} instance using the given {@link Function}. If the
     * current {@link Option} instance does not contain a value, an empty {@link Option} is returned. In all other
     * cases, the result of the given {@link Function} is returned.
     *
     * @param <U> The type of the result of the mapping function
     * @param function A mapping function to apply to the value, if present
     *
     * @return the result of applying a mapping function to the value of this {@link Option}, if a value is present,
     *         otherwise {@link Option#empty()}
     */
    @NonNull <U> Option<U> flatMap(@NonNull Function<@NonNull T, @NonNull Option<U>> function);

    /**
     * If a value is present in this {@link Option}, the value is applied to the given {@link Predicate}. If the
     * {@link Predicate} returns {@code true}, the current {@link Option} instance is returned. If the
     * {@link Predicate} returns {@code false}, an empty {@link Option} is returned. If no value is present, an empty
     * {@link Option} is returned.
     *
     * @param predicate the {@link Predicate} to apply to the value, if present.
     *
     * @return the current {@link Option} instance if a value is present and the {@link Predicate} returns
     */
    @NonNull
    Option<T> filter(@NonNull Predicate<@NonNull T> predicate);

    /**
     * Returns a {@link Stream} containing the value wrapped by the current {@link Option} instance, if a value is
     * present. If no value is present, an empty {@link Stream} is returned.
     *
     * @return a {@link Stream} containing the value wrapped by the current {@link Option} instance, if a value is
     *         present, otherwise an empty {@link Stream}.
     */
    @NonNull
    Stream<T> stream();

    /**
     * Returns a {@link Stream} based on the current {@link Option} instance. If a value is present, a {@link Stream}
     * is created using the provided {@link Function}. If no value is present, an empty {@link Stream} is returned.
     *
     * @param mapper the {@link Function} to use to create the {@link Stream} if a value is present.
     * @param <U> the type of the elements of the new {@link Stream}
     *
     * @return a {@link Stream} based on the current {@link Option} instance.
     */
    @NonNull
    default <U> Stream<U> stream(@NonNull Function<@NonNull T, @NonNull Stream<U>> mapper) {
        return this.stream().flatMap(mapper);
    }

    /**
     * Returns {@code true} if the value wrapped by the current {@link Option} instance is equal to the given value,
     * otherwise {@code false}. If no value is present, {@code false} is returned. This checks for equality, not for
     * identity.
     *
     * @param value the value to compare to the value wrapped by the current {@link Option} instance.
     * @return {@code true} if the value wrapped by the current {@link Option} instance is equal to the given value,
     *         otherwise {@code false}.
     */
    boolean contains(@Nullable T value);

    /**
     * Casts the value wrapped by the current {@link Option} instance to the given type. If the value is not of the
     * given type, an empty {@link Option} is returned. If no value is present, an empty {@link Option} is returned.
     *
     * @param type the type to cast the value to
     * @param <U> the type to cast the value to
     *
     * @return an {@link Option} containing the value wrapped by the current {@link Option} instance, cast to the given
     *       type.
     */
    default <U> Option<U> ofType(@NonNull Class<U> type) {
        return this.filter(type::isInstance).cast(type);
    }

    /**
     * Casts the value wrapped by the current {@link Option} instance to the given type. If the value is not of the
     * given type, a {@link ClassCastException} is thrown. If no value is present, an empty {@link Option} is returned.
     *
     * @param type the type to cast the value to
     * @param <U> the type to cast the value to
     *
     * @return an {@link Option} containing the value wrapped by the current {@link Option} instance, cast to the given
     *        type.
     *
     * @throws ClassCastException if the value is not of the given type
     */
    default <U> Option<U> cast(@NonNull Class<U> type) {
        return this.map(type::cast);
    }

    /**
     * Casts the value wrapped by the current {@link Option} instance to the given type. If the value is not of the
     * given type, an empty {@link Option} is returned. If no value is present, an empty {@link Option} is returned.
     * The wildcards of the given type are adjusted to match the wildcards of the provided type parameter {@link A}.
     *
     * @param type the type to cast the value to
     * @param <K> the type to cast the value to
     * @param <A> the type parameter to adjust the wildcards of the given type to
     *
     * @return an {@link Option} containing the value wrapped by the current {@link Option} instance, cast to the given type.
     */
    default <K extends T, A extends K> Option<A> adjust(@NonNull Class<K> type) {
        return this.ofType(type).map(value -> TypeUtils.unchecked(value, type));
    }

    /**
     * Tests the value wrapped by the current {@link Option} instance using the given {@link Predicate}. If a value is
     * present, the result of the {@link Predicate} is returned. If no value is present, {@code false} is returned.
     *
     * <p>This is particularly useful as a convenience method when returning primitive {@code boolean} values directly
     * from a {@link Option}, as it does not require explicit unboxing on the caller's end.
     *
     * @param predicate the {@link Predicate} to test the value with.
     *
     * @return the result of the {@link Predicate} if a value is present, otherwise {@code false}.
     */
    default boolean test(@NonNull Predicate<T> predicate) {
        return Boolean.TRUE.equals(this.map(predicate::test).orElse(false));
    }

    /**
     * Performs a mutable reduction operation on the elements of this {@link Option} using a {@link Collector}. A mutable
     * reduction is one in which the reduced value is a mutable result container, such as an {@code ArrayList}, and the
     * accumulation function mutates it by adding new elements. This produces a result equivalent to using
     * {@link Stream#collect(Collector)} through {@link #stream()}.
     *
     * @param collector the {@link Collector} describing the reduction operation
     * @param <E> the type of the result
     *
     * @return the result of the reduction
     */
    default <E> E collect(@NonNull Collector<T, ?, E> collector) {
        return this.stream().collect(collector);
    }

    /**
     * Similar to {@link #collect(Collector)}, but using explicit functions instead of a {@link Collector}. This
     * produces a result equivalent to using {@link Stream#collect(Supplier, BiConsumer, BiConsumer)} through
     * {@link #stream()}.
     *
     * @param supplier
     *         the supplier function that provides a new mutable result container.
     * @param accumulator
     *         an associative, non-interfering, stateless function for incorporating an additional element
     *         into a result container.
     * @param combiner
     *         an associative, non-interfering, stateless function for combining two values, which must be
     *         compatible with the accumulator function.
     * @param <E>
     *         the type of the result
     *
     * @return the result of the reduction
     */
    default <E> E collect(@NonNull Supplier<E> supplier, @NonNull BiConsumer<E, T> accumulator, @NonNull BiConsumer<E, E> combiner) {
        return this.stream().collect(supplier, accumulator, combiner);
    }

    /**
     * Returns a {@link List} containing the value wrapped by the current {@link Option} instance. If no value is
     * present, an empty {@link List} is returned.
     *
     * @return a {@link List} containing the value wrapped by the current {@link Option} instance.
     */
    default List<T> toList() {
        return this.collect(Collectors.toList());
    }

    /**
     * Returns a {@link Set} containing the value wrapped by the current {@link Option} instance. If no value is
     * present, an empty {@link Set} is returned.
     *
     * @return a {@link Set} containing the value wrapped by the current {@link Option} instance.
     */
    default Set<T> toSet() {
        return this.collect(Collectors.toSet());
    }

    /**
     * Returns a {@link Map} containing the value wrapped by the current {@link Option} instance as value, identified
     * by the key returned by the given {@link Function}. If no value is present in the current {@link Option} instance,
     * an empty {@link Map} is returned.
     *
     * @param keyMapper
     *         the {@link Function} used to map the value wrapped by the current {@link Option} instance to a
     *         key in the resulting {@link Map}.
     * @param <K>
     *         the type of the key in the resulting {@link Map}.
     *
     * @return a {@link Map} containing the value wrapped by the current {@link Option} instance as value, identified
     *         by the key returned by the given {@link Function}.
     */
    default <K> Map<K, T> toMap(@NonNull Function<T, K> keyMapper) {
        return this.collect(Collectors.toMap(keyMapper, Function.identity()));
    }

    /**
     * Returns a {@link Map} containing the value wrapped by the current {@link Option} as both key and value,
     * identified by the key returned by the given {@link Function}, and the value returned by the given
     * {@link Function}. If no value is present in the current {@link Option} instance, an empty {@link Map} is
     * returned.
     *
     * @param keyMapper
     *         the {@link Function} used to map the value wrapped by the current {@link Option} instance to a
     *         key in the resulting {@link Map}.
     * @param valueMapper
     *         the {@link Function} used to map the value wrapped by the current {@link Option} instance to
     *         a value in the resulting {@link Map}.
     * @param <K>
     *         the type of the keys in the resulting {@link Map}.
     * @param <V>
     *         the type of the values in the resulting {@link Map}.
     *
     * @return a {@link Map} containing the wrapped value as both key and value, as returned by the given
     *         {@link Function functions}.
     */
    default <K, V> Map<K, V> toMap(@NonNull Function<T, K> keyMapper, @NonNull Function<T, V> valueMapper) {
        return this.collect(Collectors.toMap(keyMapper, valueMapper));
    }

    @Override
    default Spliterator<T> spliterator() {
        int size = this.map(value -> 1).orElse(0);
        return Spliterators.spliterator(this.iterator(), size, 0);
    }

    @Override
    default Iterator<T> iterator() {
        return this.map(List::of)
                .map(List::iterator)
                .orElseGet(Collections::emptyIterator);
    }

    @Override
    @NonNull
    String toString(); // Force implementation of toString
}
