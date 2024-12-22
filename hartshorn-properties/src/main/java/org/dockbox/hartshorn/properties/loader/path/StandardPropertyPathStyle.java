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

package org.dockbox.hartshorn.properties.loader.path;

import java.util.regex.Pattern;

/**
 * Standard implementation of {@link PropertyPathStyle} that uses a dot as field separator and square brackets for
 * indices.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class StandardPropertyPathStyle implements PropertyPathStyle {

    public static final PropertyPathStyle INSTANCE = new StandardPropertyPathStyle();

    private static final Pattern INDEX_PATTERN = Pattern.compile("\\[(\\d+)\\]");

    @Override
    public char fieldSeparator() {
        return '.';
    }

    @Override
    public String[] resolveFields(String path) {
        return path.split("\\.");
    }

    @Override
    public String index(int index) {
        return "[%d]".formatted(index);
    }

    @Override
    public String[] resolveIndexes(String path) {
        return INDEX_PATTERN
                .matcher(path)
                .results()
                .map(result -> result.group(1)) // Group 0 is the entire match, group 1 is the index
                .toArray(String[]::new);
    }
}
