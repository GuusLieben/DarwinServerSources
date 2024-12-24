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
import org.dockbox.hartshorn.properties.parse.ConfiguredPropertyParser;
import org.dockbox.hartshorn.util.introspect.convert.Converter;
import org.dockbox.hartshorn.util.option.Option;

/**
 * A parser to convert single-value {@link ConfiguredProperty} instances to instances of a specific type using a
 * {@link Converter}.
 *
 * @param <T> the type to convert the value to
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class ConverterConfiguredPropertyParser<T> implements ConfiguredPropertyParser<T> {

    private final Converter<String, T> converter;

    public ConverterConfiguredPropertyParser(Converter<String, T> converter) {
        this.converter = converter;
    }

    @Override
    public Option<T> parse(ConfiguredProperty property) {
        return property.value().map(this.converter::convert);
    }
}
