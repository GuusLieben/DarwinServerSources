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
import org.dockbox.hartshorn.inject.processing.ComponentProcessor;
import org.dockbox.hartshorn.inject.processing.ComponentProcessorRegistry;
import org.dockbox.hartshorn.inject.provider.ComponentObjectContainer;
import org.dockbox.hartshorn.inject.provider.ObjectContainer;
import org.dockbox.hartshorn.inject.provider.PostProcessingComponentProvider;
import org.dockbox.hartshorn.util.ApplicationException;
import org.dockbox.hartshorn.util.TypeUtils;
import org.dockbox.hartshorn.util.option.Option;

/**
 * A {@link ComponentProviderStrategy} which attempts to provide a {@link ComponentProcessor} instance from the {@link
 * ComponentProcessorRegistry} of the {@link PostProcessingComponentProvider} if the requested component is a {@link
 * ComponentProcessor}.
 *
 * @see PostProcessingComponentProvider#processorRegistry()
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class ComponentProcessorComponentProviderStrategy implements ComponentProviderStrategy {

    @Override
    public <T> ObjectContainer<T> get(
            ComponentKey<T> componentKey,
            ComponentRequestContext requestContext,
            ComponentProviderStrategyChain<T> chain
    ) throws ComponentResolutionException, ApplicationException {
        if (ComponentProcessor.class.isAssignableFrom(componentKey.type())
                && chain.application().defaultProvider() instanceof PostProcessingComponentProvider postProcessingComponentProvider) {

            Class<? extends ComponentProcessor> processorType = TypeUtils.unchecked(componentKey.type(), Class.class);
            Option<? extends ComponentProcessor> processor = postProcessingComponentProvider.processorRegistry().lookup(processorType);
            // If absent, the processor may not yet have been initialized, so we'll try to process it instead of exiting early
            if (processor.present()) {
                T instance = componentKey.type().cast(processor.get());
                ObjectContainer<T> container = ComponentObjectContainer.ofSingleton(instance);
                container.processed(true);
                return container;
            }
        }
        return chain.get(componentKey, requestContext);
    }
}
