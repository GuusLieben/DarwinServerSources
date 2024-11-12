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

package org.dockbox.hartshorn.test;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.SequencedCollection;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.dockbox.hartshorn.inject.populate.ComponentPopulator;
import org.dockbox.hartshorn.inject.populate.StrategyComponentPopulator;
import org.dockbox.hartshorn.launchpad.ApplicationContext;
import org.dockbox.hartshorn.launchpad.launch.ApplicationBuilder;
import org.dockbox.hartshorn.launchpad.launch.StandardApplicationBuilder;
import org.dockbox.hartshorn.launchpad.launch.StandardApplicationContextFactory;
import org.dockbox.hartshorn.test.annotations.CustomizeTests;
import org.dockbox.hartshorn.test.annotations.TestProperties;
import org.dockbox.hartshorn.test.junit.HartshornIntegrationTest;
import org.dockbox.hartshorn.test.junit.HartshornJUnitIntegrationTestBootstrapCallback;
import org.dockbox.hartshorn.util.ApplicationException;
import org.dockbox.hartshorn.util.Customizer;
import org.dockbox.hartshorn.util.SimpleSingleElementContext;
import org.dockbox.hartshorn.util.option.Option;

/**
 * Initializes the test environment for integration tests. This class is responsible for invoking any
 * custom test environment modifiers, preparing the application factory and creating the application context,
 * and performing additional injection on the test instance if necessary.
 *
 * @see HartshornJUnitIntegrationTestBootstrapCallback
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class HartshornIntegrationTestInitializer {

    /**
     * Creates a new application context for the given test class, test instance and component sources.
     *
     * @param testClass the test class
     * @param testInstance the test instance, may be {@code null} for class lifecycle tests
     * @param testComponentSources the component sources to use for the test
     * @return the created application context
     * @throws ApplicationException when the application context could not be created
     */
    @NonNull
    public ApplicationContext createTestApplicationContext(
        Class<?> testClass,
        Object testInstance,
        AnnotatedElement[] testComponentSources
    ) throws ApplicationException {
        if (testClass == null) {
            throw new IllegalArgumentException("Test class cannot be null");
        }

        List<AnnotatedElement> elements = new ArrayList<>(Arrays.asList(testComponentSources));
        elements.add(testClass);

        this.invokeModifiers(testClass);

        ApplicationBuilder<?> applicationBuilder = this.prepareFactory(testClass, elements);
        ApplicationContext applicationContext = applicationBuilder.create();

        if (applicationContext == null) {
            throw new IllegalStateException("Could not create application context");
        }

        if (testInstance != null) {
            this.populateTestInstance(testInstance, applicationContext);
        }
        return applicationContext;
    }

    private void invokeModifiers(Class<?> testClass) throws ApplicationException {
        List<Method> methods = Arrays.stream(testClass.getMethods())
                .filter(method -> method.isAnnotationPresent(CustomizeTests.class))
                .toList();

        for (Method factoryModifier : methods) {
            doCheckFactoryModifierValid(factoryModifier);

            if (!factoryModifier.canAccess(null)) {
                factoryModifier.setAccessible(true);
            }

            Class<?>[] parameters = factoryModifier.getParameterTypes();
            if (parameters.length == 0) {

                try {
                    factoryModifier.invoke(null);
                }
                catch (Exception e) {
                    throw new ApplicationException(e);
                }
            }
            else {
                throw new InvalidFactoryModifierException("Invalid parameter count for " + factoryModifier.getName() + ", expected 0, got " + parameters.length + ".");
            }

            factoryModifier.setAccessible(false);
        }
    }

    protected void populateTestInstance(Object instance, ApplicationContext applicationContext) {
        SimpleSingleElementContext<ApplicationContext> elementContext = SimpleSingleElementContext.create(applicationContext);
        ComponentPopulator populator = StrategyComponentPopulator.create(Customizer.useDefaults()).initialize(elementContext);
        populator.populate(instance);
    }

    private ApplicationBuilder<?> prepareFactory(Class<?> testClass, List<AnnotatedElement> testComponentSources) {
        Customizer<StandardApplicationBuilder.Configurer> builderCustomizer = Customizer.useDefaults();
        builderCustomizer = builderCustomizer.compose(builder -> {
            customizeBuilderWithTestSources(testClass, testComponentSources, builder);

            Customizer<StandardApplicationContextFactory.Configurer> customizer = new IntegrationTestApplicationFactoryCustomizer(testClass, testComponentSources);
            builder.applicationContextFactory(StandardApplicationContextFactory.create(customizer.compose(TestCustomizer.CONSTRUCTOR.customizer())));
        });

        return StandardApplicationBuilder.create(builderCustomizer.compose(TestCustomizer.BUILDER.customizer()));
    }

    private static void doCheckFactoryModifierValid(Method factoryModifier) {
        if (!Modifier.isStatic(factoryModifier.getModifiers())) {
            throw new InvalidFactoryModifierException("Expected " + factoryModifier.getName() + " to be static.");
        }

        if (!factoryModifier.getReturnType().equals(Void.TYPE)) {
            throw new InvalidFactoryModifierException("Invalid return type for " + factoryModifier.getName() + ", expected void");
        }
    }

    private static void customizeBuilderWithTestSources(
            Class<?> testClass,
            SequencedCollection<AnnotatedElement> testComponentSources,
            StandardApplicationBuilder.Configurer builder
    ) {

        // Note: initial default, may be overwritten by test component sources below
        builder.mainClass(testClass);

        for (AnnotatedElement element : testComponentSources) {
            if (element == null) {
                continue;
            }

            Option.of(element.getAnnotation(HartshornIntegrationTest.class))
                    .map(HartshornIntegrationTest::mainClass)
                    .filter(mainClass -> mainClass != Void.class)
                    .peek(builder::mainClass);

            Option.of(element.getAnnotation(TestProperties.class))
                    .map(TestProperties::value)
                    .peek(properties -> builder.arguments(args -> args.addAll(properties)));
        }
    }
}
