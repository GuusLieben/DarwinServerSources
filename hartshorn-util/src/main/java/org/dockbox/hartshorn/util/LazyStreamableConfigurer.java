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

import java.util.List;

/**
 * A wrapper around {@link StreamableConfigurer} which allows for lazy configuration of the
 * {@link StreamableConfigurer} instance. This is useful when the configuration of the
 * {@link StreamableConfigurer} is dependent on the input of the {@link ContextualInitializer}.
 *
 * @param <I> the type of the input
 * @param <O> the type of the output
 *
 * @see StreamableConfigurer
 * @see ContextualInitializer
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public class LazyStreamableConfigurer<I, O> implements ContextualInitializer<I, List<O>> {

    private final StreamableConfigurer<I, O> configurer;
    private Customizer<StreamableConfigurer<I, O>> customizer = Customizer.useDefaults();

    /**
     * Creates a new {@link LazyStreamableConfigurer} instance. The configurer will be used when the
     * {@link #initialize(SingleElementContext)} method is invoked.
     *
     * @param configurer the configurer to use
     */
    public LazyStreamableConfigurer(StreamableConfigurer<I, O> configurer) {
        this.configurer = configurer;
    }

    /**
     * Adds a customizer to the {@link StreamableConfigurer} instance. The customizer is applied when the
     * {@link #initialize(SingleElementContext)} method is invoked.
     *
     * @param customizer the customizer to apply
     * @return this instance
     */
    public LazyStreamableConfigurer<I, O> customizer(Customizer<StreamableConfigurer<I, O>> customizer) {
        // Note order, existing customizer is applied first, so the new customizer can override it if needed
        this.customizer = customizer.compose(this.customizer);
        return this;
    }

    @Override
    public List<O> initialize(SingleElementContext<? extends I> input) {
        this.customizer.configure(this.configurer);
        return this.configurer.initialize(input);
    }

    /**
     * Creates a new {@link LazyStreamableConfigurer} instance with an empty {@link StreamableConfigurer}.
     *
     * @param <I> the type of the input
     * @param <O> the type of the output
     *
     * @return the new instance
     */
    public static <I, O> LazyStreamableConfigurer<I, O> empty() {
        return new LazyStreamableConfigurer<>(StreamableConfigurer.empty());
    }

    /**
     * Creates a new {@link LazyStreamableConfigurer} instance with the given {@link Customizer}. The customizer is
     * applied when the {@link #initialize(SingleElementContext)} method is invoked. The initial {@link
     * StreamableConfigurer} instance is empty.
     *
     * @param customizer the customizer to apply
     * @param <I> the type of the input
     * @param <O> the type of the output
     *
     * @return the new instance
     */
    public static <I, O> LazyStreamableConfigurer<I, O> of(Customizer<StreamableConfigurer<I, O>> customizer) {
        return new LazyStreamableConfigurer<I, O>(StreamableConfigurer.empty()).customizer(customizer);
    }

    /**
     * Creates a new {@link LazyStreamableConfigurer} instance with a {@link StreamableConfigurer} initialized with the
     * given {@link ContextualInitializer}.
     *
     * @param initializer the initializer to use
     * @param <I> the type of the input
     * @param <O> the type of the output
     *
     * @return the new instance
     */
    public static <I, O> LazyStreamableConfigurer<I, O> ofInitializer(ContextualInitializer<I, O> initializer) {
        return new LazyStreamableConfigurer<>(StreamableConfigurer.ofInitializer(initializer));
    }

    /**
     * Creates a new {@link LazyStreamableConfigurer} instance with a {@link StreamableConfigurer} initialized with the
     * given objects.
     *
     * @param objects the objects to use
     * @param <I> the type of the input
     * @param <O> the type of the output
     *
     * @return the new instance
     */
    @SafeVarargs
    public static <I, O> LazyStreamableConfigurer<I, O> of(O... objects) {
        return new LazyStreamableConfigurer<>(StreamableConfigurer.of(objects));
    }

    /**
     * Creates a new {@link LazyStreamableConfigurer} instance with a {@link StreamableConfigurer} initialized with the
     * given objects.
     *
     * @param objects the objects to use
     * @param <I> the type of the input
     * @param <O> the type of the output
     *
     * @return the new instance
     */
    public static <I, O> LazyStreamableConfigurer<I, O> of(Iterable<? extends O> objects) {
        return new LazyStreamableConfigurer<>(StreamableConfigurer.of(objects));
    }
}
