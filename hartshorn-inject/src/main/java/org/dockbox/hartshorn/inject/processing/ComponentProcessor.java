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

import org.dockbox.hartshorn.inject.ComponentKey;
import org.dockbox.hartshorn.inject.provider.InstantiationStrategy;
import org.dockbox.hartshorn.util.ApplicationException;

/**
 * The {@link ComponentProcessor} is a service that can be used to process components, whether
 * that is during the initialization of the framework, or after a component has been created.
 *
 * <p>This interface defines the basic contract for a component processor. All processors should
 * have an activator annotation, and a phase at which they are performed.
 *
 * @since 0.4.7
 *
 * @author Guus Lieben
 */
public sealed interface ComponentProcessor permits ComponentPostProcessor, ComponentPreProcessor {

    /**
     * Processes a given component context. The given context should contain the application context, the component
     * type and key, and an optional component instance. The instance may be null if the component is not yet created,
     * or if the component could not be created through regular {@link InstantiationStrategy providers}. The component
     * {@link ComponentKey} will always be valid, and will contain a valid {@link Class}. The {@link
     * org.dockbox.hartshorn.util.introspect.view.TypeView} of the context will always be valid, and will always use
     * the type providing the most amount of context. If the component instance is present, the type of that instance
     * will be used. If the component instance is not present, the type of the {@link ComponentKey} is used instead.
     *
     * @param processingContext The context of the component being processed. This contains the application context and
     *                          introspection details.
     * @return The processed component instance.
     * @param <T> The type of the component.
     */
    <T> T process(ComponentProcessingContext<T> processingContext) throws ApplicationException;

    /**
     * Returns the priority of the component processor. The priority is used to determine the order in which the component
     * processors are executed. The lower the priority, the earlier the processor will be executed.
     *
     * @return The phase of when the component should be processed.
     */
    int priority();
}
