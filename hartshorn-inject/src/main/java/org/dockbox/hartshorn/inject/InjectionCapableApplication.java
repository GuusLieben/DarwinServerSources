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

import org.dockbox.hartshorn.context.Context;
import org.dockbox.hartshorn.inject.binding.HierarchicalBinder;
import org.dockbox.hartshorn.inject.provider.ComponentProvider;

/**
 * Represents an application that is capable of injection. In other words, an application that can
 * provide components and resolve dependencies.
 *
 * @since 0.6.0
 *
 * @author Guus Lieben
 */
public interface InjectionCapableApplication extends Context {

    InjectorEnvironment environment();

    ComponentProvider defaultProvider();

    HierarchicalBinder defaultBinder();
}
