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

package org.dockbox.hartshorn.launchpad.properties;

import org.dockbox.hartshorn.launchpad.environment.ApplicationEnvironment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify the property sources to load. This annotation can be used on any class that is managed by the
 * IoC container.
 *
 * <p>Property sources do not have to be files, but can be any kind of source that can be used to load properties. Paths
 * can be absolute or relative, or an identifier that can be used to load the properties.
 *
 * <p>For example, the following paths are valid by default:
 * <ul>
 *     <li>{@code classpath:application.properties}, through {@link org.dockbox.hartshorn.launchpad.resources.ClassPathResourceLookupStrategy}</li>
 *     <li>{@code fs:/etc/application.properties}, through {@link org.dockbox.hartshorn.launchpad.resources.FileSystemLookupStrategy}</li>
 * </ul>
 *
 * <p>When multiple sources are specified, the order in which they are specified is the order in which they are loaded.
 *
 * @see ApplicationEnvironment#resourceLookup()
 * @see TypeDiscoveryPropertySourceResolver
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PropertiesSource {

    /**
     * The property sources to load. The order in which they are specified is the order in which they are loaded.
     *
     * @return the property sources
     */
    String[] value();
}
