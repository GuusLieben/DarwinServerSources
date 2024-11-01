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

package org.dockbox.hartshorn.inject.graph;

import org.dockbox.hartshorn.inject.CompositeQualifier;
import org.dockbox.hartshorn.inject.component.ComponentContainer;
import org.dockbox.hartshorn.inject.graph.declaration.DependencyDeclarationContext;
import org.dockbox.hartshorn.util.introspect.view.TypeView;

/**
 * TODO: #1060 Add documentation
 *
 * @param container ...
 * @param <T> ...
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public record ComponentContainerDependencyDeclarationContext<T>(ComponentContainer<T> container)
        implements DependencyDeclarationContext<T> {

    @Override
    public TypeView<T> type() {
        return this.container.type();
    }

    @Override
    public CompositeQualifier qualifier() {
        return new CompositeQualifier();
    }

}
