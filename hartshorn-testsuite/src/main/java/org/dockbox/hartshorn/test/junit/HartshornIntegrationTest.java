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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.inject.annotations.Inject;
import org.dockbox.hartshorn.inject.annotations.Populate;
import org.dockbox.hartshorn.inject.processing.ComponentPostProcessor;
import org.dockbox.hartshorn.inject.processing.ComponentPreProcessor;
import org.dockbox.hartshorn.inject.processing.ComponentProcessor;
import org.dockbox.hartshorn.inject.processing.HierarchicalBinderPostProcessor;
import org.dockbox.hartshorn.launchpad.ApplicationContext;
import org.dockbox.hartshorn.launchpad.activation.ServiceActivator;
import org.dockbox.hartshorn.launchpad.launch.StandardApplicationBuilder;
import org.dockbox.hartshorn.launchpad.launch.StandardApplicationContextFactory;
import org.dockbox.hartshorn.util.Customizer;
import org.dockbox.hartshorn.util.introspect.annotations.Extends;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;

/**
 * Annotation for test classes that should be run with the Hartshorn test suite. This will automatically
 * provide the test class with Hartshorn-specific {@link Extension}s that will provide an active {@link
 * InjectionCapableApplication} for the test class. The provided {@link InjectionCapableApplication} is
 * refreshed according to the lifecycle of the test class.
 *
 * <p>Additionally, the extensions will inject fields annotated with {@link Inject} within the test class.
 * Like the active {@link ApplicationContext}, this will be refreshed according to the lifecycle of the
 * test class.
 *
 * @see HartshornInjectParameterResolver
 * @see HartshornJUnitCleanupCallback
 */
@Target({ ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({
        HartshornInjectParameterResolver.class,
        HartshornJUnitCleanupCallback.class,
        HartshornJUnitIntegrationTestBootstrapCallback.class,
})
@Extends(Populate.class)
@Populate(Populate.Type.FIELDS)
public @interface HartshornIntegrationTest {

    /**
     * Additional {@link ComponentProcessor}s to use when creating the {@link ApplicationContext} for
     * the test class or method. These will be added to the default set of {@link ComponentProcessor}s
     * used by the test suite, even if they are not found through prefix scanning.
     *
     * @see StandardApplicationContextFactory.Configurer#componentPreProcessors(Customizer)
     * @see StandardApplicationContextFactory.Configurer#componentPostProcessors(Customizer)
     *
     * @return the additional {@link ComponentProcessor}s to use
     *
     * @deprecated see {@link ServiceActivator#processors()}
     */
    @Deprecated(since = "0.7.0", forRemoval = true)
    Class<? extends ComponentProcessor>[] processors() default {};

    /**
     * Additional {@link ComponentPreProcessor}s to use when creating the {@link ApplicationContext} for
     * the test class or method. These will be added to the default set of {@link ComponentPreProcessor}s
     * used by the test suite, even if they are not found through prefix scanning.
     *
     * @see StandardApplicationContextFactory.Configurer#componentPreProcessors(Customizer)
     *
     * @return the additional {@link ComponentPreProcessor}s to use
     */
    Class<? extends ComponentPreProcessor>[] componentPreProcessors() default {};

    /**
     * Additional {@link ComponentPostProcessor}s to use when creating the {@link ApplicationContext} for
     * the test class or method. These will be added to the default set of {@link ComponentPostProcessor}s
     * used by the test suite, even if they are not found through prefix scanning.
     *
     * @see StandardApplicationContextFactory.Configurer#componentPostProcessors(Customizer)
     *
     * @return the additional {@link ComponentPostProcessor}s to use
     */
    Class<? extends ComponentPostProcessor>[] componentPostProcessors() default {};

    /**
     * Additional {@link HierarchicalBinderPostProcessor}s to use when creating the {@link ApplicationContext}
     * for the test class or method. These will be added to the default set of {@link HierarchicalBinderPostProcessor}s
     * used by the test suite, even if they are not found through prefix scanning.
     *
     * @see StandardApplicationContextFactory.Configurer#binderPostProcessors(Customizer)
     *
     * @return the additional {@link HierarchicalBinderPostProcessor}s to use
     */
    Class<? extends HierarchicalBinderPostProcessor>[] binderPostProcessors() default {};

    /**
     * Additional packages to scan for {@link ComponentProcessor}s when creating the {@link ApplicationContext}
     * for the test class or method. These will be added to the default set of packages scanned by the
     * test suite, even if they are not known to the chosen {@link #mainClass()}.
     *
     * @see StandardApplicationContextFactory.Configurer#scanPackages(Customizer)
     *
     * @return the additional packages to scan
     */
    String[] scanPackages() default {};

    /**
     * Whether to include the base package of the main class explicitly, or to only use the prefixes provided to
     * {@link #scanPackages()}. Defaults to {@code true}.
     *
     * @see StandardApplicationContextFactory.Configurer#includeBasePackages(boolean)
     *
     * @return whether to include the base package of the main class
     */
    boolean includeBasePackages() default true;

    /**
     * Sets the main class to use. Depending on the type of application environment, this may require the
     * class to have relevant {@link ServiceActivator service activators}. Alternative metadata sources may be
     * used, depending on the application environment.
     *
     * @see StandardApplicationBuilder.Configurer#mainClass(Class)
     *
     * @return the main class to use, or {@link Void} if not set
     */
    Class<?> mainClass() default Void.class;
}
