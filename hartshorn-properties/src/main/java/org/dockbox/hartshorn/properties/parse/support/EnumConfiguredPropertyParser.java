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

package org.dockbox.hartshorn.properties.parse.support;

import org.dockbox.hartshorn.properties.ConfiguredProperty;
import org.dockbox.hartshorn.util.introspect.convert.support.StringToEnumConverterFactory;

/**
 * A parser to convert single-value {@link ConfiguredProperty} instances to enum instances. This parser uses the
 * {@link StringToEnumConverterFactory} to convert the string value to the target enum type.
 *
 * @param <E> the enum type to convert to
 *
 * @see StringToEnumConverterFactory
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class EnumConfiguredPropertyParser<E extends Enum<E>> extends ConverterConfiguredPropertyParser<E> {

    public EnumConfiguredPropertyParser(Class<E> type) {
        super(new StringToEnumConverterFactory().create(type));
    }
}
