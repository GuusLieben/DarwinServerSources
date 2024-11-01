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

import org.dockbox.hartshorn.inject.ComponentKey;
import org.dockbox.hartshorn.inject.ComponentRequestContext;
import org.dockbox.hartshorn.inject.ComponentResolutionException;
import org.dockbox.hartshorn.inject.provider.InstantiationStrategy;
import org.dockbox.hartshorn.inject.provider.ObjectContainer;
import org.dockbox.hartshorn.inject.provider.SimpleConstructorViewDrivenProvider;
import org.dockbox.hartshorn.util.ApplicationException;
import org.dockbox.hartshorn.util.option.Option;

/**
 * A {@link ComponentProviderStrategy} that attempts to provide a component using a {@link InstantiationStrategy prototype instantiation
 * strategy}. If the strategy is unable to provide an instance, the chain is continued.
 *
 * @see SimpleConstructorViewDrivenProvider#forPrototype(ComponentKey)
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class UnboundPrototypeComponentProviderStrategy implements ComponentProviderStrategy {

    @Override
    public <T> ObjectContainer<T> get(ComponentKey<T> componentKey, ComponentRequestContext requestContext,
            ComponentProviderStrategyChain<T> chain) throws ComponentResolutionException, ApplicationException {
        InstantiationStrategy<T> strategy = SimpleConstructorViewDrivenProvider.forPrototype(componentKey);
        Option<ObjectContainer<T>> container = strategy.provide(chain.application(), requestContext);
        if (container.present()) {
            return container.get();
        } else {
            return chain.get(componentKey, requestContext);
        }
    }
}
