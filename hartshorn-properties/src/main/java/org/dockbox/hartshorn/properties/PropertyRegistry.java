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

package org.dockbox.hartshorn.properties;

import java.util.List;
import java.util.function.Function;

import org.dockbox.hartshorn.properties.value.ValuePropertyParser;
import org.dockbox.hartshorn.util.option.Option;

public interface PropertyRegistry {

    List<String> keys();

    Option<ValueProperty> get(String name);

    Option<ObjectProperty> object(String name);

    Option<ListProperty> list(String name);

    // mapper used if value is (e.g.) a,b,c instead of 'proper' list
    Option<ListProperty> list(String name, Function<ValueProperty, ListProperty> singleValueMapper);

    default Option<String> value(String name) {
        return this.get(name).flatMap(ValueProperty::value);
    }

    default <T> Option<T> value(String name, ValuePropertyParser<T> parser) {
        return this.get(name).flatMap(parser::parse);
    }

    void register(ValueProperty property);

    void unregister(String name);

    void clear();

    boolean contains(String name);
}
