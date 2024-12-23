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

package org.dockbox.hartshorn.inject.introspect;

import org.dockbox.hartshorn.inject.annotations.PropertyValue;
import org.dockbox.hartshorn.inject.populate.InjectPropertyParameterResolver;
import org.dockbox.hartshorn.inject.provider.ComponentProvider;

/**
 * A parameter loader rule that loads parameters annotated with {@link PropertyValue}. Values are resolved from the {@link
 * org.dockbox.hartshorn.properties.PropertyRegistry}, or from the {@link PropertyValue#defaultValue()} if no value is found.
 *
 * @see InjectPropertyParameterResolver
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class PropertyParameterLoaderRule extends InjectParameterResolverParameterLoaderRule {

    public PropertyParameterLoaderRule(ComponentProvider componentProvider) {
        super(new InjectPropertyParameterResolver(componentProvider));
    }
}
