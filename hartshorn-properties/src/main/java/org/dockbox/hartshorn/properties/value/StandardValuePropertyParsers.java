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

package org.dockbox.hartshorn.properties.value;

import org.dockbox.hartshorn.properties.ValueProperty;
import org.dockbox.hartshorn.properties.value.support.ConverterValuePropertyParser;
import org.dockbox.hartshorn.properties.value.support.GenericConverterValuePropertyParser;
import org.dockbox.hartshorn.util.introspect.convert.support.StringToArrayConverter;
import org.dockbox.hartshorn.util.introspect.convert.support.StringToBooleanConverter;
import org.dockbox.hartshorn.util.introspect.convert.support.StringToCharacterConverter;
import org.dockbox.hartshorn.util.introspect.convert.support.StringToNumberConverterFactory;

/**
 * A collection of standard {@link ValuePropertyParser} instances for common types.
 */
public final class StandardValuePropertyParsers {

    private StandardValuePropertyParsers() {
        // Static access only
    }

    public static final ValuePropertyParser<Boolean> BOOLEAN = new ConverterValuePropertyParser<>(new StringToBooleanConverter());

    public static final ValuePropertyParser<Integer> INTEGER = new ConverterValuePropertyParser<>(new StringToNumberConverterFactory().create(Integer.class));

    public static final ValuePropertyParser<Long> LONG = new ConverterValuePropertyParser<>(new StringToNumberConverterFactory().create(Long.class));

    public static final ValuePropertyParser<Double> DOUBLE = new ConverterValuePropertyParser<>(new StringToNumberConverterFactory().create(Double.class));

    public static final ValuePropertyParser<Float> FLOAT = new ConverterValuePropertyParser<>(new StringToNumberConverterFactory().create(Float.class));

    public static final ValuePropertyParser<String> STRING = ValueProperty::value;

    public static final ValuePropertyParser<Character> CHARACTER = new ConverterValuePropertyParser<>(new StringToCharacterConverter());

    public static final ValuePropertyParser<Short> SHORT = new ConverterValuePropertyParser<>(new StringToNumberConverterFactory().create(Short.class));

    public static final ValuePropertyParser<Byte> BYTE = new ConverterValuePropertyParser<>(new StringToNumberConverterFactory().create(Byte.class));

    public static final ValuePropertyParser<String[]> STRING_LIST = new GenericConverterValuePropertyParser<>(new StringToArrayConverter(), String[].class);
}
