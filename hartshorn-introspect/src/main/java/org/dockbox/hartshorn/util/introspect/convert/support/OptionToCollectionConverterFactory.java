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

package org.dockbox.hartshorn.util.introspect.convert.support;

import org.dockbox.hartshorn.util.introspect.Introspector;
import org.dockbox.hartshorn.util.introspect.convert.Converter;
import org.dockbox.hartshorn.util.introspect.convert.ConverterFactory;
import org.dockbox.hartshorn.util.introspect.convert.DefaultValueProviderFactory;
import org.dockbox.hartshorn.util.option.Option;

import java.util.Collection;

public class OptionToCollectionConverterFactory implements ConverterFactory<Option<?>, Collection<?>> {

    private final DefaultValueProviderFactory<Collection<?>> defaultValueProviderFactory;

    public OptionToCollectionConverterFactory(final Introspector introspector) {
        this(new CollectionDefaultValueProviderFactory(introspector).withDefaults());
    }

    public OptionToCollectionConverterFactory(final DefaultValueProviderFactory<Collection<?>> defaultValueProviderFactory) {
        this.defaultValueProviderFactory = defaultValueProviderFactory;
    }

    @Override
    public <O extends Collection<?>> Converter<Option<?>, O> create(final Class<O> targetType) {
        return input -> {
            //noinspection unchecked
            final Collection<Object> collection = (Collection<Object>) this.defaultValueProviderFactory.create(targetType).defaultValue();
            if (input.present()) {
                collection.add(input.get());
            }
            return targetType.cast(collection);
        };
    }
}
