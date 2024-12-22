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

import java.nio.file.Path;

/**
 * Utility class for file operations.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public final class FileUtilities {

    private FileUtilities() {
        // Utility class
    }

    /**
     * Checks if the provided path has a file extension.
     *
     * @param path the path to check
     * @return {@code true} if the path has a file extension, {@code false} otherwise
     */
    public static boolean hasFileExtension(Path path) {
        return getFileExtension(path) != null;
    }

    /**
     * Returns the file extension of the provided path. If the path does not have a file extension, {@code null} is
     * returned.
     *
     * @param path the path to check
     * @return the file extension of the provided path, or {@code null} if the path does not have a file extension
     */
    public static String getFileExtension(Path path) {
        if (path.toFile().isDirectory()) {
            return null;
        }
        String fileName = path.getFileName().toString();
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return null;
        }
        return fileName.substring(index + 1);
    }

    /**
     * Checks if the provided path exists.
     *
     * @param path the path to check
     * @return {@code true} if the path exists, {@code false} otherwise
     */
    public static boolean exists(Path path) {
        return path.toFile().exists();
    }
}
