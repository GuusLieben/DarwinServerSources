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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.dockbox.hartshorn.inject.ComponentKey;
import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.inject.component.ComponentContainer;
import org.dockbox.hartshorn.inject.populate.ComponentPopulator;
import org.dockbox.hartshorn.util.ContextualInitializer;
import org.dockbox.hartshorn.util.Customizer;

/**
 * A {@link ComponentPopulatorPostProcessor} that is aware of {@link ComponentContainer} instances and their proxying
 * capabilities. This post-processor will only permit proxying if the {@link ComponentContainer} permits it.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class ContainerAwareComponentPopulatorPostProcessor extends ComponentPopulatorPostProcessor {

    @SuppressWarnings("rawtypes")
    private static final ComponentKey<ComponentContainer> COMPONENT_CONTAINER = ComponentKey.of(ComponentContainer.class);

    public ContainerAwareComponentPopulatorPostProcessor(ComponentPopulator componentPopulator) {
        super(componentPopulator);
    }

    @Override
    protected <T> boolean permitsProxying(InjectionCapableApplication application, @Nullable T instance,
            ComponentProcessingContext<T> processingContext) {
        if (!super.permitsProxying(application, instance, processingContext)) {
            return false;
        }
        return !processingContext.containsKey(COMPONENT_CONTAINER) || processingContext.get(COMPONENT_CONTAINER).permitsProxying();
    }

    public static ContextualInitializer<InjectionCapableApplication, ComponentPostProcessor> create(Customizer<Configurer> customizer) {
        return context -> {
            Configurer configurer = new Configurer();
            customizer.configure(configurer);
            return new ContainerAwareComponentPopulatorPostProcessor(configurer.componentPopulator.initialize(context));
        };
    }
}
