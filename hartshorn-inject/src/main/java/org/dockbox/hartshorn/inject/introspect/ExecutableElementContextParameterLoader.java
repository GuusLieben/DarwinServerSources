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

import org.dockbox.hartshorn.inject.ComponentKey;
import org.dockbox.hartshorn.inject.ComponentRequestContext;
import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.inject.annotations.Required;
import org.dockbox.hartshorn.inject.populate.ComponentRequiredException;
import org.dockbox.hartshorn.inject.targets.InjectionPoint;
import org.dockbox.hartshorn.util.introspect.util.RuleBasedParameterLoader;
import org.dockbox.hartshorn.util.introspect.view.AnnotatedElementView;
import org.dockbox.hartshorn.util.introspect.view.ParameterView;

/**
 * TODO: #1060 Add documentation
 *
 * @since 0.4.9
 *
 * @author Guus Lieben
 */
public class ExecutableElementContextParameterLoader extends RuleBasedParameterLoader<ApplicationBoundParameterLoaderContext> {

    private final InjectionCapableApplication application;

    public ExecutableElementContextParameterLoader(InjectionCapableApplication application) {
        super(ApplicationBoundParameterLoaderContext.class);
        this.application = application;
        this.add(new ContextParameterLoaderRule(application));
        this.add(new PropertyParameterLoaderRule(application.defaultProvider()));
    }

    @Override
    protected <T> T loadDefault(ParameterView<T> parameter, int index, ApplicationBoundParameterLoaderContext context, Object... args) {
        ComponentKey<?> componentKey = this.application.environment().componentKeyResolver().resolve(parameter);

        ComponentRequestContext requestContext = ComponentRequestContext.createForInjectionPoint(new InjectionPoint(parameter));
        Object out = context.provider().get(componentKey, requestContext);

        boolean required = isRequired(parameter);

        if (required && out == null) {
            throw new ComponentRequiredException("Parameter " + parameter.name() + " on " + parameter.declaredBy().qualifiedName() + " is required");
        }
        return parameter.type().cast(out);
    }

    private static boolean isRequired(AnnotatedElementView parameter) {
        return parameter.annotations().get(Required.class)
                .test(Required::value);
    }
}
