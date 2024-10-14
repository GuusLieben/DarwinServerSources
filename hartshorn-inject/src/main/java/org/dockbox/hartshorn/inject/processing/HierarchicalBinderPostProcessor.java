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

import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.inject.binding.HierarchicalBinder;
import org.dockbox.hartshorn.inject.scope.Scope;

/**
 * A post processor for hierarchical binders. This can be used to add additional functionality to a binder, or to modify
 * the binder in some way. The post processor will be called for each binder that is created, and will be called
 * in the order of the specified {@link HierarchicalBinderPostProcessor#priority()} value.
 *
 * <p>Explicit component instantiation is not recommended in this processor, as this processor may be called at any time
 * in the application lifecycle, including during the initialization of the application.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public interface HierarchicalBinderPostProcessor {

    void process(InjectionCapableApplication application, Scope scope, HierarchicalBinder binder);

    int priority();
}
