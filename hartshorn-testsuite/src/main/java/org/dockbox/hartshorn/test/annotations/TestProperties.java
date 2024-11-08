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
 * Arguments to be passed to the application context when running the test. Each argument is equivalent
 * to a single argument passed to the JVM when starting the application.
 *
 * <p>For example, to pass an application property, use {@code
 * @TestProperties({"--hartshorn.banner.enabled=false"})
 * }
 *
 * @since 0.4.12
 *
 * @author Guus Lieben
 */
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestProperties {

    /**
     * The properties to pass to the application context when running the test.
     *
     * @return The properties to pass to the application context when running the test
     */
    String[] value();
}
