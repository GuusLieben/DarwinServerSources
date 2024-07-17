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

package org.dockbox.hartshorn.util;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.dockbox.hartshorn.util.option.Option;

/**
 * A collection of utility methods for working with strings.
 *
 * @since 0.4.10
 *
 * @author Guus Lieben
 */
public final class StringUtilities {

    /**
     * Pattern for matching a string that represents a duration in seconds. The string must be a
     * single unsigned long.
     */
    private static final Pattern minorTimeString = Pattern.compile("^\\d+$");

    /**
     * Pattern for matching a string that represents a complex duration according to a simplified
     * ISO-8601 format. The string must be a sequence of one or more of the following:
     * <ul>
     *     <li>Unsigned long followed by 'w' for weeks</li>
     *     <li>Unsigned long followed by 'd' for days</li>
     *     <li>Unsigned long followed by 'h' for hours</li>
     *     <li>Unsigned long followed by 'm' for minutes</li>
     *     <li>Unsigned long followed by 's' for seconds</li>
     * </ul>
     * The string must not contain any other characters, including whitespace.
     *
     * <p>As this is a simplified format, months and years are not supported.
     *
     * <p>Examples:
     * <ul>
     *     <li>1w: 1 week</li>
     *     <li>1w2d: 1 week, 2 days</li>
     *     <li>1w2d3h: 1 week, 2 days, 3 hours</li>
     *     <li>1w2d3h4m: 1 week, 2 days, 3 hours, 4 minutes</li>
     *     <li>1w2d3h4m5s: 1 week, 2 days, 3 hours, 4 minutes, 5 seconds</li>
     *     <li>1d4m: 1 day, 4 minutes</li>
     * </ul>
     */
    private static final Pattern timeString = Pattern.compile("^((\\d+)w)?((\\d+)d)?((\\d+)h)?((\\d+)m)?((\\d+)s)?$");
    private static final int secondsInMinute = 60;
    private static final int secondsInHour = 60 * StringUtilities.secondsInMinute;
    private static final int secondsInDay = 24 * StringUtilities.secondsInHour;
    private static final int secondsInWeek = 7 * StringUtilities.secondsInDay;

    private StringUtilities() {
        // Utility class
    }

    /**
     * Capitalizes the first letter of the given string. If the string is empty, the string is
     * returned as-is.
     *
     * @param value the string to capitalize
     *
     * @return the capitalized string
     */
    public static String capitalize(String value) {
        return StringUtilities.empty(value)
                ? value
                : (value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1));
    }

    /**
     * Returns {@code true} if the given string is {@code null} or empty.
     *
     * @param value the string to check
     * @return {@code true} if the given string is {@code null} or empty
     */
    public static boolean empty(CharSequence value) {
        return null == value || value.isEmpty();
    }

    /**
     * Returns {@code true} if the given string is not {@code null} and not empty.
     *
     * @param value the string to check
     * @return {@code true} if the given string is not {@code null} and not empty
     */
    public static boolean notEmpty(CharSequence value) {
        return null != value && !value.isEmpty();
    }

    /**
     * Returns an empty string if the given string is {@code null}, otherwise returns the given
     * string.
     *
     * @param value the string to check
     * @return an empty string if the given string is {@code null}, otherwise returns the given
     *         string
     */
    public static String emptyIfNull(String value) {
        return null == value ? "" : value;
    }

    /**
     * Returns {@code null} if the given string is empty, otherwise returns the given string.
     *
     * @param value the string to check
     * @return {@code null} if the given string is empty, otherwise returns the given string.
     */
    public static String nullIfEmpty(String value) {
        return StringUtilities.empty(value) ? null : value;
    }

    /**
     * Strips all whitespace from the given string. This includes newlines, carriage returns, tabs,
     * and spaces.
     *
     * @param value the string to strip
     * @return the stripped string
     */
    public static String strip(String value) {
        return value.replaceAll("[\n\r\t ]+", "").trim();
    }

    /**
     * Returns a second-precision duration representing the given string. The string must be an
     * unsigned long if only seconds are represented, or a sequence of one or more of the following:
     * <ul>
     *     <li>Unsigned long followed by 'w' for weeks</li>
     *     <li>Unsigned long followed by 'd' for days</li>
     *     <li>Unsigned long followed by 'h' for hours</li>
     *     <li>Unsigned long followed by 'm' for minutes</li>
     *     <li>Unsigned long followed by 's' for seconds</li>
     * </ul>
     * The string must not contain any other characters, including whitespace.
     *
     * @param value the string to parse
     * @return a {@link Duration} representing the given string, or {@link Option#empty()} if the
     *         string could not be parsed
     */
    public static Option<Duration> durationOf(String value) {
        // First, if just digits, return the number in seconds.

        if (StringUtilities.minorTimeString.matcher(value).matches()) {
            return Option.of(Duration.ofSeconds(Long.parseUnsignedLong(value)));
        }

        Matcher m = StringUtilities.timeString.matcher(value);
        if (m.matches()) {
            long time = StringUtilities.durationAmount(m.group(2), StringUtilities.secondsInWeek);
            time += StringUtilities.durationAmount(m.group(4), StringUtilities.secondsInDay);
            time += StringUtilities.durationAmount(m.group(6), StringUtilities.secondsInHour);
            time += StringUtilities.durationAmount(m.group(8), StringUtilities.secondsInMinute);
            time += StringUtilities.durationAmount(m.group(10), 1);

            if (0 < time) {
                return Option.of(Duration.ofSeconds(time));
            }
        }
        return Option.empty();
    }

    private static long durationAmount(@Nullable String value, int multiplier) {
        if (null != value && !value.isEmpty()) {
            return multiplier * Long.parseUnsignedLong(value);
        }
        return 0;
    }

    /**
     * Splits the given string into an array of strings, using each capital letter as a delimiter.
     * The capital letters are included in the resulting strings.
     *
     * <p>Examples:
     * <ul>
     *     <li>"HelloWorld" -> ["Hello", "World"]</li>
     *     <li>"HelloW" -> ["Hello", "W"]</li>
     *     <li>"Hello" -> ["Hello"]</li>
     * </ul>
     *
     * @param value the string to split
     * @return an array of strings, using each capital letter as a delimiter
     */
    public static String[] splitCapitals(String value) {
        return value.split("(?=\\p{Lu})");
    }

    /**
     * Trims the given string of all leading and trailing characters matching the given character.
     *
     * <p>Examples:
     * <ul>
     *     <li>" value  " -> "value"</li>
     *     <li>"$value$$" -> "value"</li>
     * </ul>
     *
     * @param trimCharacter the character to trim
     * @param value the string to trim
     * @return the trimmed string
     */
    public static String trimWith(char trimCharacter, String value) {
        int length = value.length();
        int currentIndex = 0;
        char[] characters = value.toCharArray();

        while ((currentIndex < length) && (characters[currentIndex] <= trimCharacter)) {
            currentIndex++;
        }
        while ((currentIndex < length) && (characters[length - 1] <= trimCharacter)) {
            length--;
        }
        return ((currentIndex > 0) || (length < value.length())) ? value.substring(currentIndex, length) : value;
    }

    /**
     * Formats the given string using the given arguments. The string must contain placeholders in
     * the form of {@code {0}}, {@code {1}}, etc. The placeholders are replaced with the given
     * arguments in the order they are provided.
     *
     * <p>If the string contains a placeholder that is not present in the given arguments, the
     * placeholder is left as-is. If the arguments contains an index that has no corresponding
     * placeholder in the string, the argument is ignored.
     *
     * <p>Examples:
     * <ul>
     *     <li>{@code "Hello {0}", "world"} -> {@code "Hello world"}</li>
     *     <li>{@code "Hello {0}", "world", "!"} -> {@code "Hello world"} (ignores value {@code "!"})</li>
     *     <li>{@code "{0} {1}", "Hello"} -> {@code "Hello {1}"} (ignores placeholder {@code {1}})</li>
     * </ul>
     *
     * @param format the string to format
     * @param args the arguments to use for formatting
     * @return the formatted string
     */
    public static String format(String format, Object... args) {
        if (0 == args.length) {
            return format;
        }
        Map<String, String> map = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            String arg = String.valueOf(args[i]);
            map.put(String.format("{%d}", i), arg);
        }
        return StringUtilities.format(format, map);
    }

    /**
     * Formats the given string using the given replacements. The string must contain placeholders
     * matching the keys in the given map. The placeholders are replaced with the corresponding
     * values in the map. If the map contains a key that has no corresponding placeholder in the
     * string, the key is ignored.
     *
     * <p>Examples:
     * <ul>
     *     <li>{@code "Hello {name}", {"name", "world"}} -> {@code "Hello world"}</li>
     *     <li>{@code "Hello {name}", {"name", "world"}, {"!", "!"}} -> {@code "Hello world"} (ignores value {@code "!"})</li>
     *     <li>{@code "{name} {exclamation}", {"name", "Hello"}} -> {@code "Hello {exclamation}"} (ignores placeholder {@code {exclamation}})</li>
     * </ul>
     *
     * @param string the string to format
     * @param replacements the replacements to use for formatting
     * @return the formatted string
     */
    public static String format(String string, Map<String, String> replacements) {
        StringBuilder sb = new StringBuilder(string);
        int size = string.length();
        for (Entry<String, String> entry : replacements.entrySet()) {
            if (0 == size) {
                break;
            }
            String key = entry.getKey();
            String value = entry.getValue();
            int nextSearchStart;
            int start = sb.indexOf(key, 0);
            while (-1 < start) {
                int end = start + key.length();
                nextSearchStart = start + value.length();
                sb.replace(start, end, value);
                size -= end - start;
                start = sb.indexOf(key, nextSearchStart);
            }
        }
        return sb.toString();
    }

    /**
     * Joins the given elements into a string, separated by the given delimiter. The elements are
     * converted to strings using the given function. If the given collection is empty, an empty
     * string is returned.
     *
     * <p>Examples:
     * <ul>
     *     <li>{@code join(", ", Arrays.asList(1, 2, 3), String::valueOf)} -> {@code "1, 2, 3"}</li>
     *     <li>{@code join(", ", Arrays.asList("a", "b", "c"), String::toUpperCase)} -> {@code "A, B, C"}</li>
     * </ul>
     *
     * @param delimiter the delimiter to use
     * @param elements the elements to join
     * @param toStringFunction the function to convert elements to strings
     * @param <T> the type of elements to join
     *
     * @return the joined string
     */
    public static <T> String join(String delimiter, Iterable<T> elements, Function<T, String> toStringFunction) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (T element : elements) {
            if (i > 0) {
                builder.append(delimiter);
            }
            builder.append(toStringFunction.apply(element));
            i++;
        }
        return builder.toString();
    }
}
