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

import org.dockbox.hartshorn.properties.value.ValuePropertyParser;
import org.dockbox.hartshorn.properties.value.StandardPropertyParsers;
import org.dockbox.hartshorn.properties.value.support.EnumValuePropertyParser;
import org.dockbox.hartshorn.util.OptionInitializer;
import org.dockbox.hartshorn.util.SingleElementContext;
import org.dockbox.hartshorn.util.option.Option;

public class PropertyInitializer<T> implements OptionInitializer<PropertyRegistry, T> {

    private final String property;
    private final ValuePropertyParser<T> parser;

    public PropertyInitializer(String property, ValuePropertyParser<T> parser) {
        this.property = property;
        this.parser = parser;
    }

    public static <T> PropertyInitializer<T> of(String property, ValuePropertyParser<T> parser) {
        return new PropertyInitializer<>(property, parser);
    }

    public static PropertyInitializer<Boolean> booleanProperty(String property) {
        return new PropertyInitializer<>(property, StandardPropertyParsers.BOOLEAN);
    }

    public static PropertyInitializer<Integer> integerProperty(String property) {
        return new PropertyInitializer<>(property, StandardPropertyParsers.INTEGER);
    }

    public static PropertyInitializer<Long> longProperty(String property) {
        return new PropertyInitializer<>(property, StandardPropertyParsers.LONG);
    }

    public static PropertyInitializer<Double> doubleProperty(String property) {
        return new PropertyInitializer<>(property, StandardPropertyParsers.DOUBLE);
    }

    public static PropertyInitializer<Float> floatProperty(String property) {
        return new PropertyInitializer<>(property, StandardPropertyParsers.FLOAT);
    }

    public static PropertyInitializer<String> stringProperty(String property) {
        return new PropertyInitializer<>(property, StandardPropertyParsers.STRING);
    }

    public static PropertyInitializer<Character> charProperty(String property) {
        return new PropertyInitializer<>(property, StandardPropertyParsers.CHARACTER);
    }

    public static PropertyInitializer<Short> shortProperty(String property) {
        return new PropertyInitializer<>(property, StandardPropertyParsers.SHORT);
    }

    public static PropertyInitializer<Byte> byteProperty(String property) {
        return new PropertyInitializer<>(property, StandardPropertyParsers.BYTE);
    }

    public static <E extends Enum<E>> PropertyInitializer<E> enumProperty(String property, Class<E> type) {
        return new PropertyInitializer<>(property, new EnumValuePropertyParser<>(type));
    }

    @Override
    public Option<T> initialize(SingleElementContext<? extends PropertyRegistry> input) {
        PropertyRegistry registry = input.input();
        return registry.value(this.property, this.parser);
    }
}
