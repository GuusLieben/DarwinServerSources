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

package org.dockbox.hartshorn.inject.graph.resolve;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.inject.ManagedComponentEnvironment;
import org.dockbox.hartshorn.inject.binding.DefaultBindingConfigurerContext;
import org.dockbox.hartshorn.inject.component.ComponentRegistry;
import org.dockbox.hartshorn.inject.annotations.configuration.Configuration;
import org.dockbox.hartshorn.inject.condition.ConditionMatcher;
import org.dockbox.hartshorn.inject.annotations.configuration.Binds;
import org.dockbox.hartshorn.inject.graph.AbstractContainerDependencyResolver;
import org.dockbox.hartshorn.inject.graph.ComponentConfigurationException;
import org.dockbox.hartshorn.inject.graph.DependencyResolver;
import org.dockbox.hartshorn.inject.graph.declaration.DependencyContext;
import org.dockbox.hartshorn.inject.graph.declaration.DependencyDeclarationContext;
import org.dockbox.hartshorn.inject.graph.strategy.BindingStrategy;
import org.dockbox.hartshorn.inject.graph.strategy.BindingStrategyContext;
import org.dockbox.hartshorn.inject.graph.strategy.BindingStrategyRegistry;
import org.dockbox.hartshorn.inject.graph.strategy.MethodAwareBindingStrategyContext;
import org.dockbox.hartshorn.inject.graph.strategy.MethodInstanceBindingStrategy;
import org.dockbox.hartshorn.inject.graph.strategy.SimpleBindingStrategyRegistry;
import org.dockbox.hartshorn.inject.provider.ComponentProviderOrchestrator;
import org.dockbox.hartshorn.util.ContextualInitializer;
import org.dockbox.hartshorn.util.Customizer;
import org.dockbox.hartshorn.util.LazyStreamableConfigurer;
import org.dockbox.hartshorn.util.StreamableConfigurer;
import org.dockbox.hartshorn.util.introspect.view.MethodView;
import org.dockbox.hartshorn.util.introspect.view.TypeView;
import org.dockbox.hartshorn.util.option.Option;

/**
 * TODO: #1060 Add documentation
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public class BindsMethodDependencyResolver extends AbstractContainerDependencyResolver {

    private final ConditionMatcher conditionMatcher;
    private final BindingStrategyRegistry registry;
    private final ComponentRegistry componentRegistry;

    public BindsMethodDependencyResolver(ConditionMatcher conditionMatcher, ComponentRegistry componentRegistry) {
        this(conditionMatcher, componentRegistry, new SimpleBindingStrategyRegistry());
    }

    public BindsMethodDependencyResolver(ConditionMatcher conditionMatcher, ComponentRegistry componentRegistry, BindingStrategyRegistry registry) {
        this.conditionMatcher = conditionMatcher;
        this.registry = registry;
        this.componentRegistry = componentRegistry;
    }

    public BindingStrategyRegistry registry() {
        return this.registry;
    }

    @Override
    protected <T> Set<DependencyContext<?>> resolveSingle(DependencyDeclarationContext<T> declarationContext) {
        TypeView<T> componentType = declarationContext.type();
        List<? extends MethodView<T, ?>> bindsMethods = componentType.methods().annotatedWith(Binds.class);
        if (!bindsMethods.isEmpty()) {
            return this.resolveBindingMethods(declarationContext, componentType, bindsMethods);
        }
        else {
            return Set.of();
        }
    }

    @NonNull
    private <T> Set<DependencyContext<?>> resolveBindingMethods(DependencyDeclarationContext<T> componentContainer,
            TypeView<T> componentType, List<? extends MethodView<T, ?>> bindsMethods) {
        // Binds methods are only processed on managed components. If the component container is not present, there is nothing to do but check that there
        // is no incorrect usage of the @Binds annotation.
        if (this.componentRegistry.container(componentType.type()).absent()) {
            throw new IllegalStateException(
                "Component " + componentType.type().getName() + " is not a managed component, but contains binding declarations.");
        }
        else {
            if (!componentType.annotations().has(Configuration.class)){
                throw new IllegalStateException(
                    "Component " + componentType.type().getName() + " is not a configuration component, but contains binding declarations.");
            }
            return bindsMethods.stream()
                .filter(this.conditionMatcher::match)
                .flatMap(bindsMethod -> this.resolve(componentContainer, bindsMethod).stream())
                .collect(Collectors.toSet());
        }
    }

    private <T> Option<DependencyContext<?>> resolve(DependencyDeclarationContext<T> componentContainer, MethodView<T, ?> method) {
        BindingStrategyContext<T> strategyContext = new MethodAwareBindingStrategyContext<>(componentContainer, method);
        return this.registry.find(strategyContext).map(strategy -> strategy.handle(strategyContext));
    }

    public static ContextualInitializer<InjectionCapableApplication, DependencyResolver> create(Customizer<Configurer> customizer) {
        return context -> {
            Configurer configurer = new Configurer();
            customizer.configure(configurer);

            List<BindingStrategy> strategies = configurer.bindingStrategies.initialize(context);
            BindingStrategyRegistry registry = new SimpleBindingStrategyRegistry();
            strategies.forEach(registry::register);

            InjectionCapableApplication application = context.input();
            ConditionMatcher conditionMatcher = configurer.conditionMatcher.initialize(context.transform(application));
            DefaultBindingConfigurerContext.compose(context, binder -> {
                binder.bind(ConditionMatcher.class).singleton(conditionMatcher);
            });

            final ComponentRegistry componentRegistry;
            if (application.environment() instanceof ManagedComponentEnvironment environment) {
                componentRegistry = environment.componentRegistry();
            }
            else if (application.defaultProvider() instanceof ComponentProviderOrchestrator orchestrator) {
                 componentRegistry = orchestrator.componentRegistry();
            }
            else {
                throw new ComponentConfigurationException("Could not resolve component registry from current application");
            }
            return new BindsMethodDependencyResolver(conditionMatcher, componentRegistry, registry);
        };
    }

    /**
     * TODO: #1060 Add documentation
     *
     * @since 0.5.0
     *
     * @author Guus Lieben
     */
    public static class Configurer {

        private final LazyStreamableConfigurer<InjectionCapableApplication, BindingStrategy> bindingStrategies = LazyStreamableConfigurer.ofInitializer(
            MethodInstanceBindingStrategy.create(Customizer.useDefaults())
        );
        private ContextualInitializer<InjectionCapableApplication, ConditionMatcher> conditionMatcher = context -> new ConditionMatcher(context.input());

        public Configurer conditionMatcher(ConditionMatcher conditionMatcher) {
            return this.conditionMatcher(ContextualInitializer.of(conditionMatcher));
        }

        public Configurer conditionMatcher(ContextualInitializer<InjectionCapableApplication, ConditionMatcher> conditionMatcher) {
            this.conditionMatcher = conditionMatcher;
            return this;
        }

        public Configurer bindingStrategies(Customizer<StreamableConfigurer<InjectionCapableApplication, BindingStrategy>> customizer) {
            this.bindingStrategies.customizer(customizer);
            return this;
        }
    }
}
