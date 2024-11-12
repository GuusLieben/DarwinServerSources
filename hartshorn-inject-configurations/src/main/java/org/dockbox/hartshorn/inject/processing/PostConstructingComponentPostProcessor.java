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
import org.dockbox.hartshorn.inject.ComponentRequestContext;
import org.dockbox.hartshorn.inject.annotations.OnInitialized;
import org.dockbox.hartshorn.inject.graph.support.ComponentInitializationException;
import org.dockbox.hartshorn.inject.processing.construction.ComponentPostConstructor;
import org.dockbox.hartshorn.inject.provider.ObjectContainer;
import org.dockbox.hartshorn.inject.scope.Scope;
import org.dockbox.hartshorn.util.ApplicationException;

/**
 * A {@link ComponentProviderPostProcessor} that performs post-construction on components, after they have been processed
 * by a delegate {@link ComponentProviderPostProcessor}. Post-construction is performed by a {@link ComponentPostConstructor},
 * which typically invokes all {@link OnInitialized} methods on the component, though specific implementations may vary.
 *
 * @see ComponentPostConstructor
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class PostConstructingComponentPostProcessor implements ComponentProviderPostProcessor{

    private final ComponentPostConstructor postConstructor;
    private final ComponentProviderPostProcessor delegate;
    private final ComponentStoreCallback componentStoreCallback;
    private final Scope defaultScope;

    public PostConstructingComponentPostProcessor(
            ComponentPostConstructor postConstructor,
            ComponentProviderPostProcessor delegate,
            ComponentStoreCallback componentStoreCallback,
            Scope defaultScope
    ) {
        this.postConstructor = postConstructor;
        this.delegate = delegate;
        this.componentStoreCallback = componentStoreCallback;
        this.defaultScope = defaultScope;
    }

    @Override
    public <T> T processInstance(ComponentKey<T> componentKey, ObjectContainer<T> objectContainer, ComponentRequestContext requestContext)
            throws ApplicationException {
        T instance = this.delegate.processInstance(componentKey, objectContainer, requestContext);
        return this.finishComponentRequest(componentKey, objectContainer.copyForObject(instance));
    }

    private <T> T finishComponentRequest(ComponentKey<T> componentKey, ObjectContainer<T> container) {
        this.componentStoreCallback.store(componentKey, container);
        if (componentKey.postConstructionAllowed()) {
            try {
                Scope scope = componentKey.scope().orElse(this.defaultScope);
                return this.postConstructor.doPostConstruct(container.instance(), scope);
            } catch (ApplicationException e) {
                throw new ComponentInitializationException("Failed to perform post-construction on component with key " + componentKey, e);
            }
        }
        else {
            return container.instance();
        }
    }
}
