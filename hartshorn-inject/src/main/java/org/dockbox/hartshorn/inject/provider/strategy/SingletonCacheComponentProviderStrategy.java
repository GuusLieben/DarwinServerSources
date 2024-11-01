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
import org.dockbox.hartshorn.inject.provider.ComponentObjectContainer;
import org.dockbox.hartshorn.inject.provider.ComponentProvider;
import org.dockbox.hartshorn.inject.provider.ObjectContainer;
import org.dockbox.hartshorn.inject.provider.SingletonCacheComponentProvider;
import org.dockbox.hartshorn.inject.provider.singleton.SingletonCache;
import org.dockbox.hartshorn.util.ApplicationException;

/**
 * A {@link ComponentProviderStrategy} which attempts to use the {@link SingletonCache} of the {@link ComponentProvider}
 * if the provider is a {@link SingletonCacheComponentProvider}. It is assumed that all singletons stored in the {@link
 * SingletonCache} have already been processed, and do not require further enhancement.
 *
 * @see SingletonCacheComponentProvider#singletonCache()
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class SingletonCacheComponentProviderStrategy implements ComponentProviderStrategy {

    @Override
    public <T> ObjectContainer<T> get(
            ComponentKey<T> componentKey,
            ComponentRequestContext requestContext,
            ComponentProviderStrategyChain<T> chain
    ) throws ComponentResolutionException, ApplicationException {
        if (chain.componentProvider() instanceof SingletonCacheComponentProvider singletonCacheComponentProvider) {
            SingletonCache singletonCache = singletonCacheComponentProvider.singletonCache();
            if (singletonCache.contains(componentKey)) {
                T instance = singletonCache.get(componentKey)
                        .orElseThrow(() -> new ComponentResolutionException("No instance found for key " + componentKey + ", but the key was present in the singleton cache"));
                ObjectContainer<T> container = ComponentObjectContainer.ofSingleton(instance);
                container.processed(true);
                return container;
            }
        }
        return chain.get(componentKey, requestContext);
    }
}
