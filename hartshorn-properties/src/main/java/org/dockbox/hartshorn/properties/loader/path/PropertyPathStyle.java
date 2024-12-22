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

/**
 * Represents a style for formatting property paths. This can be used to customize the way fields and indices are
 * formatted in a property path.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public interface PropertyPathStyle {

    /**
     * Formats a field with the given name.
     *
     * @param name the name of the field
     * @return the formatted field
     */
    default String field(String name) {
        return "%s%s".formatted(this.fieldSeparator(), name);
    }

    /**
     * Returns the character that separates fields in a path.
     *
     * @return the field separator
     */
    char fieldSeparator();

    /**
     * Splits the given path into fields. For example, {@code path.sample.test} would return
     * {@code [path, sample, test]}.
     *
     * @param path the path to split
     * @return the fields
     */
    String[] resolveFields(String path);

    /**
     * Formats an index with the given value.
     *
     * @param index the index value
     * @return the formatted index
     */
    String index(int index);

    /**
     * Splits the given path into indices. For example, {@code path.sample[0].test[1]} would return
     * {@code [0, 1]}.
     *
     * @param path the path to split
     * @return the indices
     */
    String[] resolveIndexes(String path);
}
