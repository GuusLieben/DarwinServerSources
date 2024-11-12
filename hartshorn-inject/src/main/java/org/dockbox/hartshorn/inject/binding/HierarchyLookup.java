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

package org.dockbox.hartshorn.inject.binding;

import org.dockbox.hartshorn.inject.ComponentKey;
import org.dockbox.hartshorn.inject.scope.Scope;
import org.dockbox.hartshorn.util.collections.MultiMap;

/**
 * A resolver that can look up binding hierarchies for a given key.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public interface HierarchyLookup {

    /**
     * Returns the binding hierarchy for the given key.
     *
     * @param key the key to return the hierarchy for
     * @param <T> the type of the component
     *
     * @return the binding hierarchy
     */
    <T> BindingHierarchy<T> hierarchy(ComponentKey<T> key);

    /**
     * Returns all currently known binding hierarchies.
     *
     * @return all binding hierarchies
     */
    MultiMap<Scope, BindingHierarchy<?>> hierarchies();
}
