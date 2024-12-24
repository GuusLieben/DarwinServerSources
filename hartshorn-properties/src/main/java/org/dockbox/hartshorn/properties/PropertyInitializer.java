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
import org.dockbox.hartshorn.properties.value.StandardValuePropertyParsers;
import org.dockbox.hartshorn.properties.value.support.EnumValuePropertyParser;
import org.dockbox.hartshorn.util.OptionInitializer;
import org.dockbox.hartshorn.util.SingleElementContext;
import org.dockbox.hartshorn.util.option.Option;

/**
 * Utility {@link org.dockbox.hartshorn.util.Initializer} implementation to resolve a property value from
 * the {@link PropertyRegistry}. The value is parsed using the provided {@link ValuePropertyParser}, if a
 * value is found.
 *
 * @param <T> the type to convert the value to
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class PropertyInitializer<T> implements OptionInitializer<PropertyRegistry, T> {

    private final String property;
    private final ValuePropertyParser<T> parser;

    public PropertyInitializer(String property, ValuePropertyParser<T> parser) {
        this.property = property;
        this.parser = parser;
    }

    /**
     * Creates a new {@link PropertyInitializer} instance with the given property name and parser.
     *
     * @param property the name of the property to resolve
     * @param parser the parser to use to convert the property value
     * @param <T> the type to convert the value to
     *
     * @return a new {@link PropertyInitializer} instance
     */
    public static <T> PropertyInitializer<T> of(String property, ValuePropertyParser<T> parser) {
        return new PropertyInitializer<>(property, parser);
    }

    /**
     * Creates a new {@link PropertyInitializer} to resolve a boolean property.
     *
     * @param property the name of the property to resolve
     * @return a new {@link PropertyInitializer} instance
     */
    public static PropertyInitializer<Boolean> booleanProperty(String property) {
        return of(property, StandardValuePropertyParsers.BOOLEAN);
    }

    /**
     * Creates a new {@link PropertyInitializer} to resolve an integer property.
     *
     * @param property the name of the property to resolve
     * @return a new {@link PropertyInitializer} instance
     */
    public static PropertyInitializer<Integer> integerProperty(String property) {
        return of(property, StandardValuePropertyParsers.INTEGER);
    }

    /**
     * Creates a new {@link PropertyInitializer} to resolve a long property.
     *
     * @param property the name of the property to resolve
     * @return a new {@link PropertyInitializer} instance
     */
    public static PropertyInitializer<Long> longProperty(String property) {
        return of(property, StandardValuePropertyParsers.LONG);
    }

    /**
     * Creates a new {@link PropertyInitializer} to resolve a double property.
     *
     * @param property the name of the property to resolve
     * @return a new {@link PropertyInitializer} instance
     */
    public static PropertyInitializer<Double> doubleProperty(String property) {
        return of(property, StandardValuePropertyParsers.DOUBLE);
    }

    /**
     * Creates a new {@link PropertyInitializer} to resolve a float property.
     *
     * @param property the name of the property to resolve
     * @return a new {@link PropertyInitializer} instance
     */
    public static PropertyInitializer<Float> floatProperty(String property) {
        return of(property, StandardValuePropertyParsers.FLOAT);
    }

    /**
     * Creates a new {@link PropertyInitializer} to resolve a string property.
     *
     * @param property the name of the property to resolve
     * @return a new {@link PropertyInitializer} instance
     */
    public static PropertyInitializer<String> stringProperty(String property) {
        return of(property, StandardValuePropertyParsers.STRING);
    }

    /**
     * Creates a new {@link PropertyInitializer} to resolve a character property.
     *
     * @param property the name of the property to resolve
     * @return a new {@link PropertyInitializer} instance
     */
    public static PropertyInitializer<Character> charProperty(String property) {
        return of(property, StandardValuePropertyParsers.CHARACTER);
    }

    /**
     * Creates a new {@link PropertyInitializer} to resolve a short property.
     *
     * @param property the name of the property to resolve
     * @return a new {@link PropertyInitializer} instance
     */
    public static PropertyInitializer<Short> shortProperty(String property) {
        return of(property, StandardValuePropertyParsers.SHORT);
    }

    /**
     * Creates a new {@link PropertyInitializer} to resolve a byte property.
     *
     * @param property the name of the property to resolve
     * @return a new {@link PropertyInitializer} instance
     */
    public static PropertyInitializer<Byte> byteProperty(String property) {
        return of(property, StandardValuePropertyParsers.BYTE);
    }

    /**
     * Creates a new {@link PropertyInitializer} to resolve an enum property.
     *
     * @param property the name of the property to resolve
     * @param type the type of the enum to resolve
     * @param <E> the type of the enum to resolve
     * @return a new {@link PropertyInitializer} instance
     */
    public static <E extends Enum<E>> PropertyInitializer<E> enumProperty(String property, Class<E> type) {
        return of(property, new EnumValuePropertyParser<>(type));
    }

    @Override
    public Option<T> initialize(SingleElementContext<? extends PropertyRegistry> input) {
        PropertyRegistry registry = input.input();
        return registry.value(this.property, this.parser);
    }
}
