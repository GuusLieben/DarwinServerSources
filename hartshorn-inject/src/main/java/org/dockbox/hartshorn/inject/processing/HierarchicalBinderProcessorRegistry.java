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

package org.dockbox.hartshorn.inject.processing;

import org.dockbox.hartshorn.inject.provider.ComponentProvider;
import org.dockbox.hartshorn.util.collections.MultiMap;
import org.dockbox.hartshorn.util.option.Option;

/**
 * A registry for {@link HierarchicalBinderPostProcessor} instances. This registry is typically used by {@link ComponentProvider}s to
 * track and manage post processors that apply to instance provided by the provider.
 *
 * @see HierarchicalBinderPostProcessor
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public interface HierarchicalBinderProcessorRegistry {

    /**
     * Registers a {@link HierarchicalBinderPostProcessor} with this registry. If the processor is already registered, the
     * processor will not be added again.
     *
     * @param processor the processor to register
     */
    void register(HierarchicalBinderPostProcessor processor);

    /**
     * Unregisters a {@link HierarchicalBinderPostProcessor} from this registry. If the processor is not registered, this
     * method has no effect.
     *
     * @param processor the processor to unregister
     */
    void unregister(HierarchicalBinderPostProcessor processor);

    /**
     * Checks if a {@link HierarchicalBinderPostProcessor} exactly matching the given type is registered with this registry.
     *
     * @param componentProcessor the type of processor to check for
     * @return {@code true} if a processor of the given type is registered, {@code false} otherwise
     */
    boolean isRegistered(Class<? extends HierarchicalBinderPostProcessor> componentProcessor);

    /**
     * Looks up a {@link HierarchicalBinderPostProcessor} exactly matching the given type in this registry. If no processor
     * of the given type is registered, an empty {@link Option} is returned.
     *
     * @param componentProcessor the type of processor to look up
     * @param <T> the type of the processor
     * @return an {@link Option} containing the processor, if it is registered
     */
    <T extends HierarchicalBinderPostProcessor> Option<T> lookup(Class<T> componentProcessor);

    /**
     * Returns a {@link MultiMap} of all registered {@link HierarchicalBinderPostProcessor} instances, grouped by their priority.
     *
     * @return a {@link MultiMap} of all registered processors, grouped by priority
     */
    MultiMap<Integer, HierarchicalBinderPostProcessor> processors();
}
