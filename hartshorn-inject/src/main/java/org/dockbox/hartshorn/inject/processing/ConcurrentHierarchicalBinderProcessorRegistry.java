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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.dockbox.hartshorn.util.collections.MultiMap;
import org.dockbox.hartshorn.util.collections.MultiMapCollector;
import org.dockbox.hartshorn.util.option.Option;

/**
 * Basic implementation of a {@link HierarchicalBinderProcessorRegistry} that uses a {@link ConcurrentHashMap.KeySetView} to store
 * the registered processors.
 */
public class ConcurrentHierarchicalBinderProcessorRegistry implements HierarchicalBinderProcessorRegistry {

    private final Set<HierarchicalBinderPostProcessor> processors = ConcurrentHashMap.newKeySet();

    @Override
    public void register(HierarchicalBinderPostProcessor processor) {
        this.processors.add(processor);
    }

    @Override
    public void unregister(HierarchicalBinderPostProcessor processor) {
        this.processors.remove(processor);
    }

    @Override
    public boolean isRegistered(Class<? extends HierarchicalBinderPostProcessor> componentProcessor) {
        return this.processors.stream()
                .anyMatch(processor -> processor.getClass().equals(componentProcessor));
    }

    @Override
    public <T extends HierarchicalBinderPostProcessor> Option<T> lookup(Class<T> componentProcessor) {
        return Option.of(this.processors.stream()
                .filter(processor -> processor.getClass().equals(componentProcessor))
                .map(componentProcessor::cast)
                .findFirst());
    }

    @Override
    public MultiMap<Integer, HierarchicalBinderPostProcessor> processors() {
        return this.processors.stream()
            .collect(MultiMapCollector.toMultiMap(HierarchicalBinderPostProcessor::priority));
    }
}
