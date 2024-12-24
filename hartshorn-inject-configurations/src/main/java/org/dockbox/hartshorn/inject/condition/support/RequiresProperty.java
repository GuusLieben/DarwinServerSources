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

package org.dockbox.hartshorn.inject.condition.support;

import org.dockbox.hartshorn.inject.condition.RequiresCondition;
import org.dockbox.hartshorn.properties.PropertyRegistry;
import org.dockbox.hartshorn.util.introspect.annotations.AttributeAlias;
import org.dockbox.hartshorn.util.introspect.annotations.Extends;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A condition that requires a property to be present in the application's {@link PropertyRegistry}. The property
 * is resolved by name, and optionally by value. If the value is not specified, the property is required to be
 * present and have any value.
 *
 * @see PropertyCondition
 *
 * @since 0.4.12
 *
 * @author Guus Lieben
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Extends(RequiresCondition.class)
@RequiresCondition(condition = PropertyCondition.class)
public @interface RequiresProperty {

    /**
     * The name of the property that is required to be present.
     *
     * @return the name of the property that is required to be present
     */
    String name();

    /**
     * The value of the property that is required to be present. If not specified, the property is
     * required to be present and have any value.
     *
     * @return the value of the property that is required to be present
     */
    String withValue() default "";

    /**
     * Whether the condition should match if the property is missing. If set to {@code true}, the
     * condition will match if the property is missing. If set to {@code false}, the condition will
     * require the property to be present and optionally have a value. Typically this will be used
     * in combination with {@link #withValue()}.
     *
     * @return whether the condition should match if the property is missing
     */
    boolean matchIfMissing() default false;

    /**
     * @see RequiresCondition#failOnNoMatch()
     * @return whether to fail on no match
     */
    @AttributeAlias(value = "failOnNoMatch", target = RequiresCondition.class)
    boolean failOnNoMatch() default false;
}
