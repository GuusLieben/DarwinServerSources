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

import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.inject.provider.ComponentProvider;
import org.dockbox.hartshorn.util.collections.MultiMap;
import org.dockbox.hartshorn.util.option.Option;

/**
 * A registry for {@link ComponentProcessor} instances. This registry is used to store and manage all processors that are
 * registered within a given environment (typically a {@link ComponentProvider} or {@link InjectionCapableApplication}).
 *
 * <p>Note that the registry itself is not responsible for the actual processing of components. It is merely a storage
 * mechanism for processors.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public interface ComponentProcessorRegistry {

    void register(ComponentProcessor processor);

    void unregister(ComponentProcessor processor);

    void registryLazy(Class<? extends ComponentPostProcessor> componentProcessor);

    boolean isRegistered(Class<? extends ComponentProcessor> componentProcessor);

    <T extends ComponentProcessor> Option<T> lookup(Class<T> componentProcessor);

    Set<ComponentProcessor> processors();

    MultiMap<Integer, ComponentPostProcessor> postProcessors();

    Set<Class<? extends ComponentPostProcessor>> uninitializedPostProcessors();

    MultiMap<Integer, ComponentPreProcessor> preProcessors();

}
