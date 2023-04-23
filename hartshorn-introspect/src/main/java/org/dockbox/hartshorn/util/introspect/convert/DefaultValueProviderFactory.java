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

package org.dockbox.hartshorn.util.introspect.convert;

/**
 * A specialized {@link ConverterFactory} to handle {@code null} values. This is useful when implementing
 * default values for complex objects. This factory is used to create {@link DefaultValueProvider} instances.
 *
 * @param <T> the target type
 * @see DefaultValueProvider
 * @see ConverterFactory
 *
 * @author Guus Lieben
 * @since 23.1
 */
public interface DefaultValueProviderFactory<T> extends ConverterFactory<Null, T> {

    @Override
    <O extends T> DefaultValueProvider<O> create(Class<O> targetType);
}
