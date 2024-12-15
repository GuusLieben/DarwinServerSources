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

import org.dockbox.hartshorn.util.CollectionUtilities;
import org.dockbox.hartshorn.util.TypeUtils;
import org.dockbox.hartshorn.util.collections.ConcurrentSetTreeMultiMap;
import org.dockbox.hartshorn.util.collections.MultiMap;
import org.dockbox.hartshorn.util.collections.UnmodifiableMultiMap;
import org.dockbox.hartshorn.util.option.Option;

/**
 * Default implementation of the {@link ComponentProcessorRegistry} interface. This implementation uses {@link MultiMap}s
 * to store the registered processors.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class MultiMapComponentProcessorRegistry implements ComponentProcessorRegistry {

    private final MultiMap<Integer, ComponentPostProcessor> postProcessors = new ConcurrentSetTreeMultiMap<>();
    private final MultiMap<Integer, ComponentPreProcessor> preProcessors = new ConcurrentSetTreeMultiMap<>();
    private final Set<Class<? extends ComponentPostProcessor>> uninitializedPostProcessors = ConcurrentHashMap.newKeySet();

    @Override
    public void register(ComponentProcessor processor) {
        this.modifyProcessorRegistration(processor, MultiMap::put);
        this.uninitializedPostProcessors.remove(processor.getClass());
    }

    @Override
    public void unregister(ComponentProcessor processor) {
        this.modifyProcessorRegistration(processor, MultiMap::remove);
    }

    private <T extends ComponentProcessor> void modifyProcessorRegistration(T processor, RegistrationCallback callback) {
        int order = processor.priority();
        switch(processor) {
        case ComponentPostProcessor postProcessor:
            callback.process(TypeUtils.unchecked(this.postProcessors, MultiMap.class), order, postProcessor);
            break;
        case ComponentPreProcessor preProcessor:
            callback.process(TypeUtils.unchecked(this.preProcessors, MultiMap.class), order, preProcessor);
            break;
        default:
            throw new IllegalArgumentException("Unknown processor type: " + processor.getClass().getName());
        }
    }

    @FunctionalInterface
    private interface RegistrationCallback {
        void process(MultiMap<Integer, ? super ComponentProcessor> map, int priority, ComponentProcessor processor);
    }

    @Override
    public void registryLazy(Class<? extends ComponentPostProcessor> componentProcessor) {
        boolean alreadyInitialized = this.postProcessors.allValues().stream()
                .anyMatch(processor -> processor.getClass().equals(componentProcessor));
        if (!alreadyInitialized) {
            this.uninitializedPostProcessors.add(componentProcessor);
        }
    }

    @Override
    public boolean isRegistered(Class<? extends ComponentProcessor> componentProcessor) {
        if (this.uninitializedPostProcessors.contains(componentProcessor)) {
            return true;
        } else {
            return this.processors().stream()
                    .anyMatch(processor -> processor.getClass().equals(componentProcessor));
        }
    }

    @Override
    public <T extends ComponentProcessor> Option<T> lookup(Class<T> componentProcessor) {
        return Option.of(this.processors().stream()
                .filter(processor -> processor.getClass().equals(componentProcessor))
                .map(componentProcessor::cast)
                .findFirst());
    }

    @Override
    public Set<ComponentProcessor> processors() {
        return CollectionUtilities.merge(
                this.preProcessors.allValues(),
                this.postProcessors.allValues()
        );
    }

    @Override
    public MultiMap<Integer, ComponentPostProcessor> postProcessors() {
        return new UnmodifiableMultiMap<>(this.postProcessors);
    }

    @Override
    public Set<Class<? extends ComponentPostProcessor>> uninitializedPostProcessors() {
        return Set.copyOf(this.uninitializedPostProcessors);
    }

    @Override
    public MultiMap<Integer, ComponentPreProcessor> preProcessors() {
        return new UnmodifiableMultiMap<>(this.preProcessors);
    }
}
