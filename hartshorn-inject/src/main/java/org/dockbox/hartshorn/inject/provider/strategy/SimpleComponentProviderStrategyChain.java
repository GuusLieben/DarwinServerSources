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

package org.dockbox.hartshorn.inject.provider.strategy;

import java.util.List;

import org.dockbox.hartshorn.inject.ComponentKey;
import org.dockbox.hartshorn.inject.ComponentRequestContext;
import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.inject.graph.support.ComponentInitializationException;
import org.dockbox.hartshorn.inject.provider.ComponentObjectContainer;
import org.dockbox.hartshorn.inject.provider.ComponentProvider;
import org.dockbox.hartshorn.inject.provider.ObjectContainer;
import org.dockbox.hartshorn.util.ApplicationException;

/**
 * Basic implementation of a {@link ComponentProviderStrategyChain} that uses a list of {@link ComponentProviderStrategy
 * strategies} to resolve components.
 *
 * <p>This chain tracks the current index of the strategy being used, and will continue to the next strategy in the
 * list until the end of the list is reached. Each strategy is provided with a new chain, which allows each strategy
 * to delegate to the next strategy in the list. While this means that the chain is effectively immutable, it is not
 * recommended to re-use a chain for multiple requests.
 *
 * @param <T> the type of the component to resolve
 *
 * @see ComponentProviderStrategy
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class SimpleComponentProviderStrategyChain<T> implements ComponentProviderStrategyChain<T> {

    private final InjectionCapableApplication application;
    private final List<ComponentProviderStrategy> strategies;
    private final ComponentProvider componentProvider;
    private final int index;

    public SimpleComponentProviderStrategyChain(
            ComponentProvider componentProvider,
            InjectionCapableApplication application,
            List<ComponentProviderStrategy> strategies) {
        this(componentProvider, application, strategies, 0);
    }

    public SimpleComponentProviderStrategyChain(
            ComponentProvider componentProvider,
            InjectionCapableApplication application,
            List<ComponentProviderStrategy> strategies,
            int index
    ) {
        this.componentProvider = componentProvider;
        this.application = application;
        this.strategies = strategies;
        this.index = index;
    }

    @Override
    public ComponentProvider componentProvider() {
        return this.componentProvider;
    }

    @Override
    public InjectionCapableApplication application() {
        return this.application;
    }

    @Override
    public ObjectContainer<T> get(ComponentKey<T> componentKey, ComponentRequestContext requestContext)
            throws ComponentInitializationException, ApplicationException {
        if(this.index < this.strategies.size()) {
            ComponentProviderStrategy strategy = this.strategies.get(this.index);
            ComponentProviderStrategyChain<T> chain = new SimpleComponentProviderStrategyChain<>(
                    this.componentProvider,
                    this.application,
                    this.strategies,
                    this.index + 1
            );
            return strategy.get(componentKey, requestContext, chain);
        }
        return ComponentObjectContainer.empty();
    }
}
