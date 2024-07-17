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

package org.dockbox.hartshorn.properties.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dockbox.hartshorn.properties.ListProperty;
import org.dockbox.hartshorn.properties.Property;
import org.dockbox.hartshorn.properties.ValueProperty;
import org.dockbox.hartshorn.properties.value.ValuePropertyParser;

public class SingleValueListPropertyParser<T> implements ListPropertyParser<T> {

    private final ValuePropertyParser<T> delegate;

    public SingleValueListPropertyParser(ValuePropertyParser<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Collection<T> parse(ListProperty property) {
        List<T> values = new ArrayList<>();
        for(Property element : property.elements()) {
            if (element instanceof ValueProperty valueProperty) {
                values.add(this.delegate.parse(valueProperty).orNull());
            }
            else {
                throw new IllegalArgumentException("Expected ValueProperty, got " + element.getClass().getSimpleName());
            }
        }
        return values;
    }
}
