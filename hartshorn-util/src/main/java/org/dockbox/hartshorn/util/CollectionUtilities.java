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

package org.dockbox.hartshorn.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A collection of utility methods for working with collections. This class is not meant to be
 * instantiated.
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public final class CollectionUtilities {

    private CollectionUtilities() {
    }

    /**
     * Constructs a new unique set from the given collections. If no collections are provided an
     * empty {@link Set} is returned.
     *
     * @param collections The collections to use while constructing a new set
     * @param <T> The type of the elements in the set
     *
     * @return The new set
     *
     * @see #mergeList(Collection[])
     */
    @SafeVarargs
    public static <T> Set<T> merge(Collection<? extends T>... collections) {
        Set<T> merged = new HashSet<>();
        for (Collection<? extends T> collection : collections) {
            merged.addAll(collection);
        }
        return merged;
    }

    /**
     * Constructs a new list from the given collections. If no collections are provided an
     * empty {@link List} is returned.
     *
     * @param collections The collections to use while constructing a new list
     * @param <T> The type of the elements in the list
     *
     * @return The new list
     *
     * @see #merge(Collection[])
     */
    @SafeVarargs
    public static <T> List<T> mergeList(Collection<T>... collections) {
        List<T> merged = new ArrayList<>();
        for (Collection<T> collection : collections) {
            merged.addAll(collection);
        }
        return merged;
    }

    /**
     * Combines two arrays into a single array. The first array is copied and the second array is
     * appended to the end of the first array. The returned array is a new array and does not
     * modify the original arrays.
     *
     * @param arrayOne The first array
     * @param arrayTwo The second array
     * @param <T> The type of the elements in the arrays
     *
     * @return The merged array
     */
    public static <T> T[] merge(T[] arrayOne, T[] arrayTwo) {
        T[] merged = Arrays.copyOf(arrayOne, arrayOne.length + arrayTwo.length);
        System.arraycopy(arrayTwo, 0, merged, arrayOne.length, arrayTwo.length);
        return merged;
    }

    /**
     * Collects the difference between two collections. The returned set contains all elements
     * that are in either of the collections but not in both. The returned set is a new set and
     * does not modify the original collections.
     *
     * @param collectionOne The first collection
     * @param collectionTwo The second collection
     * @param <T> The type of the elements in the collections
     *
     * @return The difference between the two collections
     */
    public static <T> Set<T> difference(Collection<T> collectionOne, Collection<T> collectionTwo) {
        BiFunction<Collection<T>, Collection<T>, List<T>> filter = (c1, c2) -> c1.stream()
                .filter(Predicate.not(c2::contains))
                .toList();

        List<T> differenceInOne = filter.apply(collectionOne, collectionTwo);
        List<T> differenceInTwo = filter.apply(collectionTwo, collectionOne);

        List<T> mergedDifference = new ArrayList<>(differenceInOne.size() + differenceInTwo.size());
        mergedDifference.addAll(differenceInOne);
        mergedDifference.addAll(differenceInTwo);

        return Set.copyOf(mergedDifference);
    }

    /**
     * Iterates over all collections and applies the given consumer to each element in each
     * collection. Effectively this is a shorthand for {@link Collection#forEach(Consumer)}.
     *
     * @param consumer The consumer to apply to each element
     * @param <T> The type of the elements in the collections
     *
     * @param collections The collections to iterate over
     */
    @SafeVarargs
    public static <T> void forEach(Consumer<T> consumer, Collection<T>... collections) {
        for (Collection<T> collection : collections) {
            collection.forEach(consumer);
        }
    }

    /**
     * Returns a new list containing all distinct elements of the given collection. The returned
     * list is a new list and does not modify the original collection. The advantage of this
     * compared to {@link Set#of(Object...)} is that the order of the elements is preserved.
     *
     * @param collection The collection to get the distinct elements from
     * @param <T> The type of the elements in the collection
     *
     * @return The new list containing all distinct elements
     */
    public static <T> List<T> distinct(Collection<T> collection) {
        return collection.stream().distinct().toList();
    }

    /**
     * Returns the last element of the given collection. If the collection is empty or null
     * null is returned. If the collection indicates a given order, this information is used
     * to determine the last element. Otherwise the collection is iterated over and the last
     * element is returned.
     *
     * @param collection The collection to get the last element from
     * @param <T> The type of the elements in the collection
     *
     * @return The last element of the collection
     */
    @Nullable
    public static <T> T last(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        else if (collection instanceof SortedSet<T> sortedSet) {
            return sortedSet.last();
        }
        else if (collection instanceof SequencedCollection<T> sequencedCollection) {
            return sequencedCollection.getLast();
        }
        else {
            return collection.stream()
                .skip(collection.size() - 1L)
                .findFirst()
                .orElse(null);
        }
    }

    /**
     * Returns the first element of the given iterable. If the iterable is empty or null
     * null is returned. If the iterable indicates a given order, this information is used
     * to determine the first element. Otherwise the {@link Iterable#iterator() iterator}
     * is used to get the first element.
     *
     * @param iterable The collection to get the first element from
     * @param <T> The type of the elements in the collection
     *
     * @return The first element of the collection
     */
    @Nullable
    public static <T> T first(Iterable<T> iterable) {
        if (iterable == null || (iterable instanceof Collection<T> collection && collection.isEmpty())) {
            return null;
        }
        else if (iterable instanceof SortedSet<T> sortedSet) {
            return sortedSet.first();
        }
        else if (iterable instanceof SequencedCollection<T> sequencedCollection) {
            return sequencedCollection.getFirst();
        }
        else {
            return iterable.iterator().next();
        }
    }

    /**
     * Returns an aggregated string representation of the given collection. The string representation
     * is created by mapping each element of the collection to a string using the given value mapper
     * and then joining the strings using a comma and a space. If the collection is empty an empty
     * string is returned.
     *
     * @param collection The collection to create a string representation of
     * @param valueMapper The function to map each element to a string
     * @param <T> The type of the elements in the collection
     *
     * @return The aggregated string representation of the collection
     */
    public static <T> String toString(Collection<T> collection, Function<T, ?> valueMapper) {
        return collection.stream()
                .map(valueMapper)
                .map(Object::toString)
                .reduce("", (a, b) -> a + ", " + b);
    }

    /**
     * Returns a new set containing all elements of the given set. The returned set is a new set
     * and does not modify the original set. The advantage of this compared to {@link Set#copyOf(Collection)}
     * is that the order of the elements is preserved if the given set is a {@link SequencedSet}.
     *
     * @param set The set to copy
     * @param <T> The type of the elements in the set
     *
     * @return The new set containing all elements
     */
    public static <T> Set<T> copyOf(Set<T> set) {
        if (set instanceof SequencedSet<T> sequencedSet) {
            return Collections.unmodifiableSequencedSet(sequencedSet);
        }
        else {
            return Set.copyOf(set);
        }
    }

    /**
     * Iterates over the given iterator and applies the given consumer to each element. A counter
     * is used to keep track of the index of the element in the iterator. The counter starts at 0
     * and is incremented for each element in the iterator.
     *
     * @param iterator The iterator to iterate over
     * @param consumer The consumer to apply to each element
     * @param <T> The type of the elements in the iterator
     */
    public static <T> void indexed(Iterator<T> iterator, BiConsumer<Integer, T> consumer) {
        int index = 0;
        while (iterator.hasNext()) {
            consumer.accept(index++, iterator.next());
        }
    }

    /**
     * Iterates over the given {@link java.util.Map.Entry entries} and applies the given consumer
     * to each entry. This is no different from iterating over the entries and applying a consumer
     * to each entry, except for the fact that this method allows a bi-consumer to be used instead.
     *
     * @param iterator The iterator to iterate over
     * @param consumer The consumer to apply to each entry
     * @param <T> The type of the keys in the entries
     * @param <U> The type of the values in the entries
     */
    public static <T, U> void iterateEntries(Iterator<Entry<T, U>> iterator, BiConsumer<T, U> consumer) {
        while (iterator.hasNext()) {
            Entry<T, U> entry = iterator.next();
            consumer.accept(entry.getKey(), entry.getValue());
        }
    }

    public static <T, R> Stream<R> flatMapCollection(Collection<T> collection, Function<T, Collection<R>> function) {
        return collection.stream()
                .flatMap(value -> function.apply(value).stream());
    }

    public static <T, R> Stream<R> flatMapArray(Collection<T> collection, Function<T, R[]> function) {
        return collection.stream()
                .flatMap(value -> Stream.of(function.apply(value)));
    }
}
