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

import org.dockbox.hartshorn.properties.ListProperty;
import org.dockbox.hartshorn.properties.ObjectProperty;
import org.dockbox.hartshorn.properties.Property;
import org.dockbox.hartshorn.properties.ValueProperty;
import org.dockbox.hartshorn.util.option.Option;

import java.util.Collection;
import java.util.List;

/**
 * Basic implementation of {@link ListProperty} that uses a list to store {@link Property properties}.
 *
 * @param name the name of the property
 * @param elements the elements of the list
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public record SimpleListProperty(String name, List<Property> elements) implements ListProperty {

    @Override
    public List<Property> elements() {
        return List.copyOf(this.elements);
    }

    @Override
    public int size() {
        return this.elements().size();
    }

    @Override
    public Option<ValueProperty> get(int index) {
        return Option.of(this.elements().get(index)).ofType(ValueProperty.class);
    }

    @Override
    public List<ValueProperty> values() {
        return this.elements().stream()
                .filter(ValueProperty.class::isInstance)
                .map(ValueProperty.class::cast)
                .toList();
    }

    @Override
    public Option<ObjectProperty> object(int index) {
        return Option.of(this.elements().get(index)).ofType(ObjectProperty.class);
    }

    @Override
    public List<ObjectProperty> objects() {
        return this.elements().stream()
                .filter(ObjectProperty.class::isInstance)
                .map(ObjectProperty.class::cast)
                .toList();
    }

    @Override
    public Option<ListProperty> list(int index) {
        return Option.of(this.elements().get(index)).ofType(ListProperty.class);
    }

    @Override
    public List<ListProperty> lists() {
        return this.elements().stream()
                .filter(ListProperty.class::isInstance)
                .map(ListProperty.class::cast)
                .toList();
    }

    @Override
    public <T> Collection<T> parse(ListPropertyParser<T> parser) {
        return parser.parse(this);
    }
}
