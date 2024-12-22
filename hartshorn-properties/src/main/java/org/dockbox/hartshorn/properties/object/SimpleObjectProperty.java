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

package org.dockbox.hartshorn.properties.object;

import org.dockbox.hartshorn.properties.ListProperty;
import org.dockbox.hartshorn.properties.ObjectProperty;
import org.dockbox.hartshorn.properties.Property;
import org.dockbox.hartshorn.properties.ValueProperty;
import org.dockbox.hartshorn.util.option.Option;

import java.util.Map;

/**
 * Basic implementation of {@link ObjectProperty} that uses a map to store {@link Property properties}.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class SimpleObjectProperty extends AbstractMapObjectProperty<Property> {

    public SimpleObjectProperty(String name, Map<String, Property> properties) {
        super(name, properties);
    }

    @Override
    public Option<ValueProperty> get(String name) {
        return this.property(name).ofType(ValueProperty.class);
    }

    @Override
    public Option<ObjectProperty> object(String name) {
        return this.property(name).ofType(ObjectProperty.class);
    }

    @Override
    public Option<ListProperty> list(String name) {
        return this.property(name).ofType(ListProperty.class);
    }
}
