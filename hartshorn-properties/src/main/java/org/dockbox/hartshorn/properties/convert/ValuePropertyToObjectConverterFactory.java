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

package org.dockbox.hartshorn.properties.convert;

import org.dockbox.hartshorn.properties.ValueProperty;
import org.dockbox.hartshorn.util.introspect.convert.ConditionalConverter;
import org.dockbox.hartshorn.util.introspect.convert.ConversionService;
import org.dockbox.hartshorn.util.introspect.convert.Converter;
import org.dockbox.hartshorn.util.introspect.convert.ConverterFactory;

/**
 * Converts an {@link ValueProperty} to an {@link Object}. If the {@link ValueProperty} does not contain a value,
 * {@code null} is returned. Otherwise, the value of the {@link ValueProperty} is converted to the target type.
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public class ValuePropertyToObjectConverterFactory implements ConverterFactory<ValueProperty, Object>, ConditionalConverter {

    private final ConversionService conversionService;

    public ValuePropertyToObjectConverterFactory(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public <O> Converter<ValueProperty, O> create(Class<O> targetType) {
        return property -> {
            assert property != null;
            return property.value()
                    .map(value -> this.conversionService.convert(value, targetType))
                    .orNull();
        };
    }

    @Override
    public boolean canConvert(Object source, Class<?> targetType) {
        if (source instanceof ValueProperty property) {
            return property.value().present();
        }
        return false;
    }
}
