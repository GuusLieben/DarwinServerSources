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

package org.dockbox.hartshorn.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test method or class as requiring specific components to be registered in the test context.
 * These components will be available for injection in the test method or class.
 *
 * @since 0.4.11
 *
 * @author Guus Lieben
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface TestComponents {

    /**
     * Components to register for the test. These components will be registered in the test context
     * and will be available for injection in the test method or class.
     *
     * @return The components to register for the test
     */
    Class<?>[] components() default {};

    /**
     * Static bindings to register for the test.
     *
     * @return The static bindings to register for the test
     * @see TestBinding
     * @deprecated Use configuration classes instead
     */
    @Deprecated(since = "0.7.0", forRemoval = true)
    TestBinding[] bindings() default {};
}
