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
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class AbstractDelegateStream<T> implements Stream<T> {

    private Stream<T> delegate;

    protected AbstractDelegateStream(Stream<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public AbstractDelegateStream<T> filter(Predicate<? super T> predicate) {
        this.delegate = this.delegate.filter(predicate);
        return this;
    }

    @Override
    public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        return this.delegate.map(mapper);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return this.delegate.mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return this.delegate.mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return this.delegate.mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return this.delegate.flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return this.delegate.flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return this.delegate.flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return this.delegate.flatMapToDouble(mapper);
    }

    @Override
    public AbstractDelegateStream<T> distinct() {
        this.delegate = this.delegate.distinct();
        return this;
    }

    @Override
    public AbstractDelegateStream<T> sorted() {
        this.delegate = this.delegate.sorted();
        return this;
    }

    @Override
    public AbstractDelegateStream<T> sorted(Comparator<? super T> comparator) {
        this.delegate = this.delegate.sorted(comparator);
        return this;
    }

    @Override
    public AbstractDelegateStream<T> peek(Consumer<? super T> action) {
        this.delegate = this.delegate.peek(action);
        return this;
    }

    @Override
    public AbstractDelegateStream<T> limit(long maxSize) {
        this.delegate = this.delegate.limit(maxSize);
        return this;
    }

    @Override
    public AbstractDelegateStream<T> skip(long n) {
        this.delegate = this.delegate.skip(n);
        return this;
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        this.delegate.forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        this.delegate.forEachOrdered(action);
    }

    @Override
    public Object[] toArray() {
        return this.delegate.toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return this.delegate.toArray(generator);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return this.delegate.reduce(identity, accumulator);
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return this.delegate.reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return this.delegate.reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return this.delegate.collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return this.delegate.collect(collector);
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return this.delegate.min(comparator);
    }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return this.delegate.max(comparator);
    }

    @Override
    public long count() {
        return this.delegate.count();
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return this.delegate.anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return this.delegate.allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return this.delegate.noneMatch(predicate);
    }

    @Override
    public Optional<T> findFirst() {
        return this.delegate.findFirst();
    }

    @Override
    public Optional<T> findAny() {
        return this.delegate.findAny();
    }

    @Override
    public Iterator<T> iterator() {
        return this.delegate.iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return this.delegate.spliterator();
    }

    @Override
    public boolean isParallel() {
        return this.delegate.isParallel();
    }

    @Override
    public AbstractDelegateStream<T> sequential() {
        this.delegate = this.delegate.sequential();
        return this;
    }

    @Override
    public AbstractDelegateStream<T> parallel() {
        this.delegate = this.delegate.parallel();
        return this;
    }

    @Override
    public AbstractDelegateStream<T> unordered() {
        this.delegate = this.delegate.unordered();
        return this;
    }

    @Override
    public AbstractDelegateStream<T> onClose(Runnable closeHandler) {
        this.delegate = this.delegate.onClose(closeHandler);
        return this;
    }

    @Override
    public void close() {
        this.delegate.close();
    }
}
