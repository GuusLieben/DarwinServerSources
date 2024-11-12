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

package org.dockbox.hartshorn.test.junit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.inject.annotations.Inject;
import org.dockbox.hartshorn.inject.introspect.ApplicationBoundParameterLoaderContext;
import org.dockbox.hartshorn.inject.introspect.ExecutableElementContextParameterLoader;
import org.dockbox.hartshorn.util.TypeUtils;
import org.dockbox.hartshorn.util.introspect.Introspector;
import org.dockbox.hartshorn.util.introspect.util.ParameterLoader;
import org.dockbox.hartshorn.util.introspect.view.MethodView;
import org.dockbox.hartshorn.util.option.Option;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * A parameter resolver for JUnit 5 that resolves parameters annotated with {@link Inject}, and optionally
 * {@code jakarta.inject.Inject} or {@code javax.inject.Inject} if they are present on the classpath.
 *
 * <p>Annotated parameters are resolved in the same manner as they would be in an executable element (methods
 * and constructors) that is managed by the IoC container.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class HartshornInjectParameterResolver implements ParameterResolver {

    private static final Option<Class<? extends Annotation>> JAVAX_INJECT = TypeUtils.forName("javax.inject.Inject", Annotation.class);
    private static final Option<Class<? extends Annotation>> JAKARTA_INJECT = TypeUtils.forName("jakarta.inject.Inject", Annotation.class);

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.isAnnotated(Inject.class)
                || JAVAX_INJECT.test(parameterContext::isAnnotated)
                || JAKARTA_INJECT.test(parameterContext::isAnnotated);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Optional<Method> testMethod = extensionContext.getTestMethod();
        if (testMethod.isEmpty()) {
            throw new ParameterResolutionException("Test method was not provided to runner");
        }

        InjectionCapableApplication application = HartshornJUnitNamespace.application(extensionContext);
        Introspector introspector = application.environment().introspector();
        MethodView<?, ?> executable = introspector.introspect(testMethod.get());

        ParameterLoader parameterLoader = new ExecutableElementContextParameterLoader(application);
        ApplicationBoundParameterLoaderContext parameterLoaderContext = new ApplicationBoundParameterLoaderContext(
                executable,
                extensionContext.getTestInstance().orElse(null),
                application
        );

        return parameterLoader.loadArgument(parameterLoaderContext, parameterContext.getIndex());
    }
}
