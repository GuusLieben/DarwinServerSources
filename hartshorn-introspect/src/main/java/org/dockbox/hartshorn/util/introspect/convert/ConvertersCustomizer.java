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

package org.dockbox.hartshorn.util.introspect.convert;

/**
 * Customizer for conversion services and associated registries. Implementations of this interface can be used to
 * customize the conversion process, for example by adding converters to the conversion service or registry.
 *
 * <p>This customizer is typically called immediately after the conversion service or registry is created, but before
 * it is released for general use.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
@FunctionalInterface
public interface ConvertersCustomizer {

    /**
     * Configure the given conversion service and its associated registry.
     *
     * @param conversionService the conversion service to configure
     * @param converterRegistry the converter registry to configure
     */
    void configure(ConversionService conversionService, ConverterRegistry converterRegistry);
}
