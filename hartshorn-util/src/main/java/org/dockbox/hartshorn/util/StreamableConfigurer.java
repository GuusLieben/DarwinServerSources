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

import org.dockbox.hartshorn.context.DefaultContext;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * A {@link Configurer} that can be used to configure a collection of objects. Each object is
 * represented by a {@link ContextualInitializer} that can be used to initialize the object
 * with a given input.
 *
 * @param <I> the type of the input
 * @param <T> the type of the output
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public class StreamableConfigurer<I, T> extends DefaultContext implements Configurer {

    private final List<ContextualInitializer<I, T>> objects = new CopyOnWriteArrayList<>();

    /**
     * Creates a new {@link StreamableConfigurer} instance. Protected to prevent direct instantiation
     * outside of extending classes and static factory methods.
     */
    protected StreamableConfigurer() {
    }

    /**
     * Creates a new {@link StreamableConfigurer} instance without any initializers.
     *
     * @param <I> the type of the input
     * @param <T> the type of the output
     *
     * @return the new instance
     */
    public static <I, T> StreamableConfigurer<I, T> empty() {
        return new StreamableConfigurer<>();
    }

    /**
     * Creates a new {@link StreamableConfigurer} instance with the given objects. The objects
     * are expected to be fully configured, and will not be modified by the configurer or a given
     * input value.
     *
     * @param objects the objects to add
     * @param <I> the type of the input
     * @param <O> the type of the output
     *
     * @return the new instance
     */
    @SafeVarargs
    public static <I, O> StreamableConfigurer<I, O> of(O... objects) {
        StreamableConfigurer<I, O> configurer = StreamableConfigurer.empty();
        return configurer.addAll(objects);
    }

    /**
     * Creates a new {@link StreamableConfigurer} instance with the given objects. The objects
     * are expected to be fully configured, and will not be modified by the configurer or a given
     * input value.
     *
     * @param objects the objects to add
     * @param <I> the type of the input
     * @param <O> the type of the output
     *
     * @return the new instance
     */
    public static <I, O> StreamableConfigurer<I, O> of(Iterable<? extends O> objects) {
        StreamableConfigurer<I, O> configurer = StreamableConfigurer.empty();
        return configurer.addAll(objects);
    }

    /**
     * Creates a new {@link StreamableConfigurer} instance with the given initializer. The initializer
     * will be added as the only initializer to the configurer.
     *
     * @param initializer the initializer to add
     * @param <I> the type of the input
     * @param <O> the type of the output
     *
     * @return the new instance
     */
    public static <I, O> StreamableConfigurer<I, O> ofInitializer(ContextualInitializer<I, O> initializer) {
        StreamableConfigurer<I, O> configurer = StreamableConfigurer.empty();
        return configurer.add(initializer);
    }

    /**
     * Adds the given object to the configurer. The object is expected to be fully configured,
     * and will not be modified by the configurer or a given input value.
     *
     * @param object the object to add
     * @return the configurer
     */
    public StreamableConfigurer<I, T> add(T object) {
        this.objects.add(ContextualInitializer.of(object));
        return this;
    }

    /**
     * Adds the given initializer to the configurer. The initializer will be wrapped in a
     * {@link ContextualInitializer} that will ignore the input value. The result of the
     * initializer is expected to be fully configured, and will not be modified by the configurer
     * or a given input value.
     *
     * @param initializer the initializer to add
     * @return the configurer
     */
    public StreamableConfigurer<I, T> add(Initializer<T> initializer) {
        this.objects.add(ContextualInitializer.of(initializer));
        return this;
    }

    /**
     * Adds the given initializer to the configurer.
     *
     * @param initializer the initializer to add
     * @return the configurer
     */
    public StreamableConfigurer<I, T> add(ContextualInitializer<I, T> initializer) {
        this.objects.add(initializer);
        return this;
    }

    /**
     * Adds all given objects to the configurer. The objects are expected to be fully configured,
     * and will not be modified by the configurer or a given input value.
     *
     * @param objects the objects to add
     * @return the configurer
     */
    @SafeVarargs
    public final StreamableConfigurer<I, T> addAll(T... objects) {
        for (T object : objects) {
            this.add(object);
        }
        return this;
    }

    public StreamableConfigurer<I, T> addAll(Collection<T> objects) {
        for (T object : objects) {
            this.add(object);
        }
        return this;
    }

    /**
     * Adds all given initializers to the configurer. The initializers will be wrapped in
     * {@link ContextualInitializer}s that will ignore the input value. The result of each
     * initializer is expected to be fully configured, and will not be modified by the configurer
     * or a given input value.
     *
     * @param initializers the initializers to add
     * @return the configurer
     */
    @SafeVarargs
    public final StreamableConfigurer<I, T> addAll(Initializer<T>... initializers) {
        for (Initializer<T> initializer : initializers) {
            this.add(initializer);
        }
        return this;
    }

    /**
     * Adds all given initializers to the configurer.
     *
     * @param initializers the initializers to add
     * @return the configurer
     */
    @SafeVarargs
    public final StreamableConfigurer<I, T> addAll(ContextualInitializer<I, T>... initializers) {
        for (ContextualInitializer<I, T> resolver : initializers) {
            this.add(resolver);
        }
        return this;
    }

    /**
     * Adds all given objects to the configurer. The objects are expected to be fully configured,
     * and will not be modified by the configurer or a given input value.
     *
     * @param objects the objects to add
     * @return the configurer
     */
    public StreamableConfigurer<I, T> addAll(Iterable<? extends T> objects) {
        for (T object : objects) {
            this.add(object);
        }
        return this;
    }

    /**
     * Removes the given initializer from the configurer. If the initializer is not present in the
     * configurer, no action is taken.
     *
     * @param initializer the initializer to remove
     * @return the configurer
     */
    public StreamableConfigurer<I, T> remove(ContextualInitializer<I, T> initializer) {
        this.objects.remove(initializer);
        return this;
    }

    /**
     * Removes all initializers from the configurer.
     *
     * @return the configurer
     */
    public StreamableConfigurer<I, T> clear() {
        this.objects.clear();
        return this;
    }

    /**
     * Returns a {@link Stream} of all initializers in the configurer.
     *
     * @return a stream of all initializers
     */
    public Stream<ContextualInitializer<I, T>> stream() {
        return this.objects.stream();
    }

    /**
     * Initializes all objects in the configurer with the given input value. No guarantees are made
     * about the order in which the objects are initialized, or about the presence of duplicates and
     * {@code null} values in the result.
     *
     * @param input the input value
     * @return a list of all initialized objects
     */
    public List<T> initialize(SingleElementContext<? extends I> input) {
        return this.stream()
                .map(resolver -> resolver.initialize(input))
                .toList();
    }
}
