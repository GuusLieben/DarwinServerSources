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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.dockbox.hartshorn.launchpad.ApplicationContext;
import org.dockbox.hartshorn.test.HartshornIntegrationTestInitializer;
import org.dockbox.hartshorn.util.ApplicationException;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * A callback that initializes the Hartshorn application context before a test lifecycle has been started. This callback
 * is responsible for creating the application context and registering it in the extension context. The application
 * context is created by a {@link HartshornIntegrationTestInitializer}.
 *
 * @see HartshornIntegrationTestInitializer
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class HartshornJUnitIntegrationTestBootstrapCallback implements
        BeforeAllCallback,
        BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) throws ApplicationException {
        Class<?> testClass = context.getTestClass().orElse(null);
        Object testInstance = context.getTestInstance().orElse(null);
        Method testMethod = context.getTestMethod().orElse(null);
        this.beforeLifecycle(context, testClass, testInstance, testMethod);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws ApplicationException {
        if (JUnitTestUtilities.isClassLifecycle(context)) {
            Class<?> testClass = context.getTestClass().orElse(null);
            Object testInstance = context.getTestInstance().orElse(null);
            this.beforeLifecycle(context, testClass, testInstance);
        }
    }

    protected void beforeLifecycle(ExtensionContext context, Class<?> testClass, Object testInstance, AnnotatedElement... testComponentSources)
            throws ApplicationException {
        HartshornIntegrationTestInitializer initializer = new HartshornIntegrationTestInitializer();
        ApplicationContext applicationContext = initializer.createTestApplicationContext(testClass, testInstance, testComponentSources);
        HartshornJUnitNamespace.application(context, applicationContext);
    }
}
