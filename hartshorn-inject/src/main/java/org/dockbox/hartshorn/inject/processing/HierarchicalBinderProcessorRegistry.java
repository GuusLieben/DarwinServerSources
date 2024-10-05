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

import org.dockbox.hartshorn.inject.scope.ScopeKey;
import org.dockbox.hartshorn.util.collections.MultiMap;
import org.dockbox.hartshorn.util.option.Option;

public interface HierarchicalBinderProcessorRegistry extends HierarchicalBinderPostProcessor{

    void register(HierarchicalBinderPostProcessor processor);

    void register(ScopeKey scope, HierarchicalBinderPostProcessor processor);

    void unregister(HierarchicalBinderPostProcessor processor);

    void unregister(ScopeKey scope, HierarchicalBinderPostProcessor processor);

    boolean isRegistered(Class<? extends HierarchicalBinderPostProcessor> componentProcessor);

    boolean isRegistered(ScopeKey scope, Class<? extends HierarchicalBinderPostProcessor> componentProcessor);

    <T extends HierarchicalBinderPostProcessor> Option<T> lookup(Class<T> componentProcessor);

    <T extends HierarchicalBinderPostProcessor> Option<T> lookup(ScopeKey scope, Class<T> componentProcessor);

    MultiMap<Integer, HierarchicalBinderPostProcessor> processors();

    MultiMap<Integer, HierarchicalBinderPostProcessor> processors(ScopeKey scope);

}
