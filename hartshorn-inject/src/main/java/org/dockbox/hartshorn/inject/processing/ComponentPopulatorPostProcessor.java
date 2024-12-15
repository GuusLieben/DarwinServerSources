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

import java.util.Collection;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.dockbox.hartshorn.inject.binding.DefaultBindingConfigurerContext;
import org.dockbox.hartshorn.inject.provider.ComponentConstructorResolver;
import org.dockbox.hartshorn.inject.ComponentKey;
import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.inject.introspect.ViewContextAdapter;
import org.dockbox.hartshorn.inject.populate.ComponentPopulator;
import org.dockbox.hartshorn.inject.populate.StrategyComponentPopulator;
import org.dockbox.hartshorn.proxy.ProxyFactory;
import org.dockbox.hartshorn.proxy.lookup.StateAwareProxyFactory;
import org.dockbox.hartshorn.util.ApplicationException;
import org.dockbox.hartshorn.util.ApplicationRuntimeException;
import org.dockbox.hartshorn.util.ContextualInitializer;
import org.dockbox.hartshorn.util.Customizer;
import org.dockbox.hartshorn.util.introspect.view.ConstructorView;
import org.dockbox.hartshorn.util.introspect.view.TypeView;

/**
 * TODO: #1060 Add documentation
 *
 * @since 0.4.11
 *
 * @author Guus Lieben
 */
public class ComponentPopulatorPostProcessor extends ComponentPostProcessor {

    @SuppressWarnings("rawtypes")
    private static final ComponentKey<ProxyFactory> PROXY_FACTORY = ComponentKey.of(ProxyFactory.class);

    private final ComponentPopulator componentPopulator;

    public ComponentPopulatorPostProcessor(ComponentPopulator componentPopulator) {
        this.componentPopulator = componentPopulator;
    }

    @Override
    public <T> T initializeComponent(InjectionCapableApplication application, @Nullable T instance, ComponentProcessingContext<T> processingContext) {
        boolean permitsProxying = this.permitsProxying(application, instance, processingContext);
        if (permitsProxying && !(instance instanceof Collection<?>)) {
            T finalizingInstance = instance;

            if (processingContext.containsKey(PROXY_FACTORY)) {
                ProxyFactory<T> factory = processingContext.get(PROXY_FACTORY);

                boolean isStateAwareFactory = factory instanceof StateAwareProxyFactory<?>;
                // If not state aware, always assume state has been modified
                boolean stateModified = !isStateAwareFactory || ((StateAwareProxyFactory<T>) factory).modified();
                boolean noConcreteInstancePossible = instance == null && processingContext.type().modifiers().isAbstract();
                try {
                    if (stateModified || noConcreteInstancePossible) {
                        finalizingInstance = this.createProxyInstance(application, factory, instance);
                    }
                }
                catch (ApplicationException e) {
                    throw new ApplicationRuntimeException(e);
                }
            }

            if (processingContext instanceof ModifiableComponentProcessingContext<T> modifiableComponentProcessingContext) {
                modifiableComponentProcessingContext.instance(finalizingInstance);
                modifiableComponentProcessingContext.requestInstanceLock();
            }

            return this.componentPopulator.populate(finalizingInstance);
        }
        return instance;
    }

    protected <T> boolean permitsProxying(InjectionCapableApplication application, @Nullable T instance,
            ComponentProcessingContext<T> processingContext) {
        return processingContext.permitsProxying();
    }

    protected <T> T createProxyInstance(InjectionCapableApplication application, ProxyFactory<T> factory, @Nullable T instance) throws ApplicationException {
        TypeView<T> factoryType = application.environment().introspector().introspect(factory.type());
        // Ensure we use a non-default constructor if there is no default constructor to use
        if (!factoryType.isInterface() && factoryType.constructors().defaultConstructor().absent()) {
            ConstructorView<? extends T> constructor = ComponentConstructorResolver.create(application).findConstructor(factoryType)
                    .orElseThrow(() -> new ApplicationException("No default or injectable constructor found for proxy factory " + factoryType.name()));

            ViewContextAdapter adapter = application.defaultProvider().get(ViewContextAdapter.class);
            Object[] arguments = adapter.loadParameters(constructor);
            return factory.proxy(constructor, arguments).orElse(instance);
        }
        return factory.proxy().orElse(instance);
    }

    @Override
    public int priority() {
        // Run after all other core post processors, but permit external post processors to run after this one
        return ProcessingPriority.LOWEST_PRECEDENCE - 128;
    }

    public static ContextualInitializer<InjectionCapableApplication, ComponentPostProcessor> create(Customizer<Configurer> customizer) {
        return context -> {
            Configurer configurer = new Configurer();
            customizer.configure(configurer);
            ComponentPopulator populator = configurer.componentPopulator.initialize(context);
            DefaultBindingConfigurerContext.compose(context, binder -> {
                binder.bind(ComponentPopulator.class).singleton(populator);
            });
            return new ComponentPopulatorPostProcessor(populator);
        };
    }

    /**
     * TODO: #1060 Add documentation
     *
     * @since 0.6.0
     *
     * @author Guus Lieben
     */
    public static class Configurer {

        ContextualInitializer<InjectionCapableApplication, ComponentPopulator> componentPopulator = StrategyComponentPopulator.create(Customizer.useDefaults());

        public Configurer componentPopulator(ComponentPopulator componentPopulator) {
            return this.componentPopulator(ContextualInitializer.of(componentPopulator));
        }

        public Configurer componentPopulator(ContextualInitializer<InjectionCapableApplication, ComponentPopulator> componentPopulator) {
            this.componentPopulator = componentPopulator;
            return this;
        }
    }
}
