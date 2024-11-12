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

package org.dockbox.hartshorn.inject;

import org.dockbox.hartshorn.inject.binding.BindingHierarchy;

/**
 * Represents the basic configuration of an {@link InjectorEnvironment}.
 *
 * @see InjectorEnvironment#configuration()
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public interface InjectorConfiguration {

    /**
     * Returns whether the injector should be in strict mode. In strict mode, the injector will require
     * all {@link BindingHierarchy binding hierarchies} to be resolved using exact matching {@link
     * ComponentKey keys}. If strict mode is disabled, the injector will attempt to resolve the hierarchy
     * using the most specific key available (loose matching).
     *
     * @return {@code true} if the injector is in strict mode, {@code false} otherwise
     */
    boolean isStrictMode();

}
