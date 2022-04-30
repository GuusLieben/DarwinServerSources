/*
 * Copyright 2019-2022 the original author or authors.
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

package org.dockbox.hartshorn.util;

import java.util.UUID;

/**
 * A collection of built-in string type adapters.
 *
 * @see StringTypeAdapter
 * @author Guus Lieben
 * @since 21.9
 */
public final class BuiltInStringTypeAdapters {

    /**
     * A basic passthrough adapter, to support dynamic usages of type adapters. Returns the given value, wrapped in a
     * {@link Exceptional}.
     */
    public static final StringTypeAdapter<String> STRING = StringTypeAdapterImpl.of(String.class, Exceptional::of);

    /**
     * A character adapter, converting a string to a {@link Character}. If the input is empty, {@link Exceptional#empty()}
     * is returned. If the input is longer than one character, the first character of the input is returned. If the input
     * is exactly one character, the character is returned.
     */
    public static final StringTypeAdapter<Character> CHARACTER = StringTypeAdapterImpl.of(Character.class, in -> {
        int length = in.length();
        return 1 == length ? Exceptional.of(in.charAt(0)) : Exceptional.empty();
    });

    /**
     * A boolean adapter, converting a string to a {@link Boolean}. If the input equals {@code yes} or {@code true}, the
     * result is {@code true}. If the input equals {@code no} or {@code false}, the result is {@code false}. Otherwise,
     * the result is always {@code false}.
     */
    public static final StringTypeAdapter<Boolean> BOOLEAN = StringTypeAdapterImpl.of(Boolean.class, in -> switch (in) {
        case "yes" -> Exceptional.of(true);
        case "no" -> Exceptional.of(false);
        default -> Exceptional.of(in).map(Boolean::parseBoolean);
    });

    /**
     * A double adapter, converting a string to an {@link Double}. If the input is empty, {@link Exceptional#empty()}
     * is returned. If the input is not a valid double, {@link Exceptional#empty()} is returned. Otherwise, the result
     * is the double value of the input as produced by {@link Double#parseDouble(String)}.
     */
    public static final StringTypeAdapter<Double> DOUBLE = StringTypeAdapterImpl.of(Double.class, in -> Exceptional.of(in).map(Double::parseDouble));

    /**
     * A float adapter, converting a string to an {@link Float}. If the input is empty, {@link Exceptional#empty()}
     * is returned. If the input is not a valid float, {@link Exceptional#empty()} is returned. Otherwise, the result
     * is the float value of the input as produced by {@link Float#parseFloat(String)}.
     */
    public static final StringTypeAdapter<Float> FLOAT = StringTypeAdapterImpl.of(Float.class, in -> Exceptional.of(in).map(Float::parseFloat));

    /**
     * An integer adapter, converting a string to an {@link Integer}. If the input is empty, {@link Exceptional#empty()}
     * is returned. If the input is not a valid integer, {@link Exceptional#empty()} is returned. Otherwise, the result
     * is the integer value of the input as produced by {@link Integer#parseInt(String)}.
     */
    public static final StringTypeAdapter<Integer> INTEGER = StringTypeAdapterImpl.of(Integer.class, in -> Exceptional.of(in).map(Integer::parseInt));

    /**
     * A long adapter, converting a string to an {@link Long}. If the input is empty, {@link Exceptional#empty()}
     * is returned. If the input is not a valid long, {@link Exceptional#empty()} is returned. Otherwise, the result
     * is the long value of the input as produced by {@link Long#parseLong(String)}.
     */
    public static final StringTypeAdapter<Long> LONG = StringTypeAdapterImpl.of(Long.class, in -> Exceptional.of(in).map(Long::parseLong));

    /**
     * A short adapter, converting a string to an {@link Short}. If the input is empty, {@link Exceptional#empty()}
     * is returned. If the input is not a valid short, {@link Exceptional#empty()} is returned. Otherwise, the result
     * is the short value of the input as produced by {@link Short#parseShort(String)}.
     */
    public static final StringTypeAdapter<Short> SHORT = StringTypeAdapterImpl.of(Short.class, in -> Exceptional.of(in).map(Short::parseShort));

    /**
     * A UUID adapter, converting a string to a {@link UUID}. If the input is a valid UUID according to the specifications
     * in {@link UUID}, the result is the UUID value of the input as produced by {@link UUID#fromString(String)}.
     */
    public static final StringTypeAdapter<UUID> UNIQUE_ID = StringTypeAdapterImpl.of(UUID.class, in -> Exceptional.of(in).map(UUID::fromString));

    private BuiltInStringTypeAdapters() {}
}