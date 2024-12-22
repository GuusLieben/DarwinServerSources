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

package org.dockbox.hartshorn.properties;

/**
 * Represents a property within the application's configuration, typically exposed through {@link PropertyRegistry
 * property registries}. Properties exist in three forms: {@link ValueProperty value properties}, {@link ListProperty
 * list properties} and {@link ObjectProperty object properties}. Each of these types of properties can be used to
 * represent different types of configuration values. As each form of property exposes data in a different way, there
 * is no value in using this interface directly. Instead, use one of the three sub-interfaces.
 *
 * @see ValueProperty
 * @see ListProperty
 * @see ObjectProperty
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public sealed interface Property permits ValueProperty, ListProperty, ObjectProperty {

    /**
     * Returns the name of the property.
     *
     * @return The name of the property
     */
    String name();
}
