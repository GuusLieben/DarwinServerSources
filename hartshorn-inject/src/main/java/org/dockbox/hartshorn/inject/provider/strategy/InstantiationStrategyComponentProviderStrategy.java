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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.dockbox.hartshorn.inject.ComponentKey;
import org.dockbox.hartshorn.inject.ComponentRequestContext;
import org.dockbox.hartshorn.inject.binding.BindingHierarchy;
import org.dockbox.hartshorn.inject.binding.NestedHierarchyLookup;
import org.dockbox.hartshorn.inject.binding.HierarchicalBinder;
import org.dockbox.hartshorn.inject.binding.HierarchyLookup;
import org.dockbox.hartshorn.inject.provider.BinderAwareComponentProvider;
import org.dockbox.hartshorn.inject.provider.ComponentProvider;
import org.dockbox.hartshorn.inject.provider.InstantiationStrategy;
import org.dockbox.hartshorn.inject.provider.ObjectContainer;
import org.dockbox.hartshorn.util.ApplicationException;
import org.dockbox.hartshorn.util.option.Option;

public class InstantiationStrategyComponentProviderStrategy implements ComponentProviderStrategy {

    @Override
    public <T> ObjectContainer<T> get(
            ComponentKey<T> componentKey,
            ComponentRequestContext requestContext,
            ComponentProviderStrategyChain<T> chain
    ) throws ApplicationException {
        BindingHierarchy<T> hierarchy = this.hierarchy(chain.componentProvider(), componentKey, true);
        if(hierarchy != null) {
            ObjectContainer<T> container = this.createFromHierarchy(componentKey, requestContext, chain, hierarchy);
            if(container != null) {
                return container;
            }
        }
        return chain.get(componentKey, requestContext);
    }

    private <T> @Nullable ObjectContainer<T> createFromHierarchy(ComponentKey<T> componentKey, ComponentRequestContext requestContext,
            ComponentProviderStrategyChain<T> chain, BindingHierarchy<T> hierarchy) throws ApplicationException {
        InstantiationStrategy<T> strategy = componentKey.strategy().selectProvider(hierarchy);
        if (strategy != null) {
            return this.createFromInstantiationStrategy(requestContext, chain, strategy);
        }
        return null;
    }

    private <T> @Nullable ObjectContainer<T> createFromInstantiationStrategy(ComponentRequestContext requestContext,
            ComponentProviderStrategyChain<T> chain, InstantiationStrategy<T> strategy) throws ApplicationException {
        Option<ObjectContainer<T>> container = strategy.provide(chain.application(), requestContext);
        return container.orNull();
    }

    private <T> BindingHierarchy<T> hierarchy(ComponentProvider componentProvider, ComponentKey<T> key, boolean useGlobalIfAbsent) {
        switch(componentProvider) {
            case HierarchyLookup hierarchyLookup -> {
                return this.hierarchy(hierarchyLookup, key, useGlobalIfAbsent);
            }
            case BinderAwareComponentProvider binderAwareProvider
                when binderAwareProvider.binder() instanceof HierarchicalBinder hierarchicalBinder -> {
                return this.hierarchy(hierarchicalBinder, key, useGlobalIfAbsent);
            }
            default -> {
                return null;
            }
        }
    }

    private <T> BindingHierarchy<T> hierarchy(HierarchyLookup lookup, ComponentKey<T> key, boolean useGlobalIfAbsent) {
        if (lookup instanceof NestedHierarchyLookup containedLookup) {
            return containedLookup.hierarchy(key, useGlobalIfAbsent);
        }
        return lookup.hierarchy(key);
    }
}
