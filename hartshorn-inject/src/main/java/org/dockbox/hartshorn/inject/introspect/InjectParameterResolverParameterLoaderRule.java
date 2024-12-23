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

package org.dockbox.hartshorn.inject.introspect;

import org.dockbox.hartshorn.inject.populate.ComponentRequiredException;
import org.dockbox.hartshorn.inject.populate.InjectParameterResolver;
import org.dockbox.hartshorn.inject.populate.PopulateComponentContext;
import org.dockbox.hartshorn.inject.targets.AnnotatedInjectionPointRequireRule;
import org.dockbox.hartshorn.inject.targets.InjectionPoint;
import org.dockbox.hartshorn.inject.targets.RequireInjectionPointRule;
import org.dockbox.hartshorn.util.TypeUtils;
import org.dockbox.hartshorn.util.introspect.util.ParameterLoaderRule;
import org.dockbox.hartshorn.util.introspect.view.ParameterView;
import org.dockbox.hartshorn.util.introspect.view.TypeView;
import org.dockbox.hartshorn.util.option.Option;

/**
 * A parameter loader rule that loads parameters based on the {@link InjectParameterResolver} provided. By default,
 * this rule also keeps track of whether the parameter is required or not, and throws a {@link ComponentRequiredException}
 * if the parameter is required but could not be resolved.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class InjectParameterResolverParameterLoaderRule implements ParameterLoaderRule<ApplicationBoundParameterLoaderContext> {

    private final RequireInjectionPointRule requireRule = new AnnotatedInjectionPointRequireRule();
    private final InjectParameterResolver parameterResolver;

    public InjectParameterResolverParameterLoaderRule(InjectParameterResolver parameterResolver) {
        this.parameterResolver = parameterResolver;
    }

    @Override
    public boolean accepts(ParameterView<?> parameter, int index, ApplicationBoundParameterLoaderContext context, Object... args) {
        return this.parameterResolver.accepts(new InjectionPoint(parameter));
    }

    private PopulateComponentContext<?> createContext(ApplicationBoundParameterLoaderContext context) {
        Object instance = context.instance();
        TypeView<?> type = context.executable().declaredBy();
        return new PopulateComponentContext<>(
                instance, instance,
                TypeUtils.unchecked(type, TypeView.class),
                context.application()
        );
    }

    @Override
    public <T> Option<T> load(ParameterView<T> parameter, int index, ApplicationBoundParameterLoaderContext context, Object... args) {
        InjectionPoint injectionPoint = new InjectionPoint(parameter);
        Object resolved = this.parameterResolver.resolve(injectionPoint, this.createContext(context));
        if (resolved == null && this.requireRule.isRequired(injectionPoint)) {
            throw new ComponentRequiredException("Parameter " + parameter.name() + " on " + parameter.declaredBy().qualifiedName() + " is required");
        }
        return Option.of(parameter.type().cast(resolved));
    }
}
