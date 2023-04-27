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
 * A factory for creating {@link Converter} instances. This is the main interface used to create
 * converters for a specific target type, which can be especially useful when working with types
 * such as {@link Number}.
 *
 * @param <I> the input type
 * @param <R> the parent of targeted types (e.g. {@link Number} for {@link Integer}, {@link Long}, etc.)
 */
public interface ConverterFactory<I, R> {

    /**
     * Returns a converter to convert from the source type to the target type, or {@code null} if
     * no converter is available.
     *
     * @param targetType the target type to convert to
     * @return the converter to convert from the source type to the target type, or {@code null} if
     * @param <O> the output type
     */
    <O extends R> Converter<I, O> create(Class<O> targetType);
}
