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
import org.dockbox.hartshorn.inject.provider.HierarchicalComponentProvider;

/**
 * A binder is used to bind types or keys to various targets, and is capable of importing existing
 * {@link BindingHierarchy binding hierarchies}.
 *
 * <p>Binders may be delegated to other binders, and may be used to create new bindings or override
 * existing bindings.
 *
 * @since 0.4.1
 *
 * @author Guus Lieben
 */
public interface Binder {

    /**
     * Open a new binding function for the given type. This will create a new binding function that
     * will bind to the given type as an unnamed key in the default scope of the binder.
     *
     * @param type The type to bind to
     * @param <C> The type of the binding
     *
     * @return The binding function
     */
    default <C> BindingFunction<C> bind(Class<C> type) {
        // Strict, so new hierarchies are created if needed, rather than using loose lookup
        ComponentKey<C> componentKey = ComponentKey.builder(type)
                .strict(true)
                .build();
        return this.bind(componentKey);
    }

    /**
     * Open a new binding function for the given key. This will create a new binding function that
     * will bind to the given key. The key may be named or unnamed, and may contain a scope.
     *
     * @param key The key to bind to
     * @return The binding function
     * @param <C> The type of the binding
     */
    <C> BindingFunction<C> bind(ComponentKey<C> key);

    /**
     * Imports the given hierarchy into the current binder. This will override any existing bindings
     * in the current binder with the bindings from the given hierarchy. If you wish to keep both
     * the existing and new bindings, obtain the active bindings from the current {@link HierarchicalComponentProvider}
     * and merge them with {@link BindingHierarchy#merge(BindingHierarchy)}.
     *
     * @param hierarchy The hierarchy to import
     * @return The binder
     * @param <C> The type of the binding
     */
    <C> Binder bind(BindingHierarchy<C> hierarchy);
}
