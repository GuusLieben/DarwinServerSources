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

package org.dockbox.hartshorn.properties.parse;

import org.dockbox.hartshorn.properties.ConfiguredProperty;

public final class StandardPropertyParsers {

    private StandardPropertyParsers() {
        // Static access only
    }

    public static final ConfiguredPropertyParser<Boolean> BOOLEAN = property -> property.value().map(Boolean::parseBoolean);

    public static final ConfiguredPropertyParser<Integer> INTEGER = property -> property.value().map(Integer::parseInt);

    public static final ConfiguredPropertyParser<Long> LONG = property -> property.value().map(Long::parseLong);

    public static final ConfiguredPropertyParser<Double> DOUBLE = property -> property.value().map(Double::parseDouble);

    public static final ConfiguredPropertyParser<Float> FLOAT = property -> property.value().map(Float::parseFloat);

    public static final ConfiguredPropertyParser<String> STRING = ConfiguredProperty::value;

    public static final ConfiguredPropertyParser<Character> CHARACTER = property -> property.value().map(value -> value.charAt(0));

    public static final ConfiguredPropertyParser<Short> SHORT = property -> property.value().map(Short::parseShort);

    public static final ConfiguredPropertyParser<Byte> BYTE = property -> property.value().map(Byte::parseByte);

    public static final ConfiguredPropertyParser<Byte> HEX_BYTE = property -> property.value().map(value -> (byte) Integer.parseInt(value, 16));

}
