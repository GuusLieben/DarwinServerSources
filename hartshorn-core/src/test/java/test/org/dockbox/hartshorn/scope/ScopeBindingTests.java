/*
 * Copyright 2019-2023 the original author or authors.
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

package test.org.dockbox.hartshorn.scope;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.ComponentKey;
import org.dockbox.hartshorn.component.Scope;
import org.dockbox.hartshorn.component.ScopeAdapter;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.dockbox.hartshorn.testsuite.TestComponents;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import test.org.dockbox.hartshorn.scope.ScopedBindingProvider.SampleScope;

@HartshornTest
public class ScopeBindingTests {

    @Inject
    private ApplicationContext applicationContext;

    @Test
    void testDefaultScopeBindingIsApplicationContext() {
        final ComponentKey<String> key = ComponentKey.builder(String.class)
                .scope(Scope.DEFAULT_SCOPE)
                .build();

        this.applicationContext.bind(key).singleton("test");
        final String value = this.applicationContext.get(String.class);
        Assertions.assertEquals("test", value);
    }

    @Test
    void testScopeBindingIsNotAccessibleFromApplication() {
        final Scope scope = ScopeAdapter.of(new Object());
        final ComponentKey<String> key = ComponentKey.builder(String.class)
                .scope(scope)
                .build();

        this.applicationContext.bind(key).singleton("test");
        final String value = this.applicationContext.get(key);
        Assertions.assertEquals("test", value);

        final ComponentKey<String> componentKeyNoScope = ComponentKey.builder(String.class)
                .scope(Scope.DEFAULT_SCOPE)
                .build();

        final String valueNoScope = this.applicationContext.get(componentKeyNoScope);
        Assertions.assertEquals("", valueNoScope); // Default value for primitives
    }

    @Test
    void testApplicationBindingIsAccessibleFromScope() {
        this.applicationContext.bind(String.class).singleton("test");

        final Scope scope = ScopeAdapter.of(new Object());
        final ComponentKey<String> key = ComponentKey.builder(String.class)
                .scope(scope)
                .build();

        final String value = this.applicationContext.get(key);
        Assertions.assertEquals("test", value);
    }

    @Test
    @TestComponents(components = ScopedBindingProvider.class)
    void name() {
        final String applicationScope = this.applicationContext.get(String.class);
        final String scopedValue = this.applicationContext.get(ComponentKey.builder(String.class).scope(new SampleScope()).build());
        Assertions.assertEquals("", applicationScope);
        Assertions.assertEquals("test", scopedValue);
    }
}
