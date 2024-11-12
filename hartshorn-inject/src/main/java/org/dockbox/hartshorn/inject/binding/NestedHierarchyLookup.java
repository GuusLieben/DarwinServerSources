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

/**
 * A {@link HierarchyLookup} that is nested in another {@link HierarchyLookup}. This allows the
 * {@link NestedHierarchyLookup} to look up bindings in the parent {@link HierarchyLookup} if the
 * binding is not found in the {@link NestedHierarchyLookup}.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public interface NestedHierarchyLookup extends HierarchyLookup {

    /**
     * Returns the {@link BindingHierarchy} for the given {@link ComponentKey}. If the binding is not
     * found in this {@link NestedHierarchyLookup} and {@code useGlobalIfAbsent} is {@code true}, the
     * parent {@link HierarchyLookup} is used to look up the binding.
     *
     * @param key the key of the hierarchy to look up
     * @param useGlobalIfAbsent whether to use the parent {@link HierarchyLookup} if the binding is not found
     * @return the {@link BindingHierarchy} for the given {@link ComponentKey}
     * @param <T> the type of the hierarchy
     */
    <T> BindingHierarchy<T> hierarchy(ComponentKey<T> key, boolean useGlobalIfAbsent);
}
