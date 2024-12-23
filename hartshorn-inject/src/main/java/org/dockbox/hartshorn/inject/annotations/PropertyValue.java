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

package org.dockbox.hartshorn.inject.annotations;

import org.dockbox.hartshorn.properties.ListProperty;
import org.dockbox.hartshorn.properties.ObjectProperty;
import org.dockbox.hartshorn.properties.ValueProperty;
import org.dockbox.hartshorn.util.introspect.annotations.Extends;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated field or parameter should be injected with a value from the {@link
 * org.dockbox.hartshorn.properties.PropertyRegistry}. If no value is found, the {@link PropertyValue#defaultValue()} is used.
 *
 * <p>Properties in the property registry may have been obtained from various sources, such as configuration files or
 * environment variables. The {@link PropertyValue} annotation is a convenient way to inject these values into components.
 * Alternatively, the {@link org.dockbox.hartshorn.properties.PropertyRegistry} can be used directly to obtain values.
 *
 * <p>Parameters with literal {@link ValueProperty}, {@link ListProperty}, or {@link ObjectProperty} types will be
 * resolved directly from the registry. Other types will be resolved in their raw form from the property registry, and
 * will be converted by the {@link org.dockbox.hartshorn.util.introspect.convert.ConversionService}.
 *
 * <p>For example, the following code snippet demonstrates how to inject a property into a component:
 * <pre>{@code
 * @Component
 * public class MyComponent {
 *
 *    @Value(name = "sample.message", defaultValue = "Hello world!")
 *    private String message;
 *
 *    @Value(name = "sample.enum", defaultValue = "FIRST")
 *    private SampleEnum enumValue;
 * }}</pre>
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Extends(Inject.class)
public @interface PropertyValue {

    /**
     * The name of the property to inject. If no property with this name is found, the {@link PropertyValue#defaultValue()} is
     * used.
     *
     * @return the name of the property to inject
     */
    String name();

    /**
     * The default value to use if no property with the name {@link PropertyValue#name()} is found.
     *
     * @return the default value to use
     */
    String defaultValue() default "";
}
