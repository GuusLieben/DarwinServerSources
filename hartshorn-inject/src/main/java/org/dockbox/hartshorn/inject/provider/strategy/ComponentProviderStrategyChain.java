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
import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.inject.graph.support.ComponentInitializationException;
import org.dockbox.hartshorn.inject.provider.ComponentProvider;
import org.dockbox.hartshorn.inject.provider.ObjectContainer;
import org.dockbox.hartshorn.util.ApplicationException;

/**
 * A top-level interface for a chain of {@link ComponentProviderStrategy} instances. This chain is typically passed to
 * the {@link ComponentProviderStrategy}, which can then use it delegate to the next strategy in the chain.
 *
 * @param <T> the type of the component to resolve
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public interface ComponentProviderStrategyChain<T> {

    ComponentProvider componentProvider();

    InjectionCapableApplication application();

    ObjectContainer<T> get(ComponentKey<T> componentKey, ComponentRequestContext requestContext)
            throws ComponentInitializationException, ApplicationException;
}
