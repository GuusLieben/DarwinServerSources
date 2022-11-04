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

package org.dockbox.hartshorn.application.scan.classpath;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class DirectoryFileTreeWalker implements FileVisitor<Path> {

    private final ClassPathScanner classPathScanner;
    private final int rootDirNameLength;
    private final ResourceHandler handler;
    private final URLClassLoader classLoader;

    public DirectoryFileTreeWalker(ClassPathScanner classPathScanner, final int rootDirNameLength, final ResourceHandler handler,
            final URLClassLoader classLoader) {
        this.classPathScanner = classPathScanner;
        this.rootDirNameLength = rootDirNameLength;
        this.handler = handler;
        this.classLoader = classLoader;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        final String resourceName = file.toFile().getCanonicalPath().substring(this.rootDirNameLength + 1);
        classPathScanner.processPathResource(this.handler, this.classLoader, resourceName, file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {
        return FileVisitResult.CONTINUE;
    }
}