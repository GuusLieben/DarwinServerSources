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

package org.dockbox.hartshorn.core.boot;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The default implementation of {@link ClasspathResourceLocator}. This implementation will copy the resource to a temporary
 * location and return the path to the temporary location.
 *
 * @author Guus Lieben
 * @since 22.1
 */
public class ClassLoaderClasspathResourceLocator implements ClasspathResourceLocator {

    private final ApplicationContext applicationContext;

    public ClassLoaderClasspathResourceLocator(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Exceptional<Path> resource(final String name) {
        return Exceptional.of(() -> Resources.getResourceAsFile(name)).map(File::toPath);
    }

    @Override
    public Set<Path> resources(final String name) {
        try {
            final String normalizedPath = name.replaceAll("\\\\", "/");
            final int lastIndex = normalizedPath.lastIndexOf("/");
            final String path = lastIndex == -1 ? "" : normalizedPath.substring(0, lastIndex + 1);
            final String fileName = lastIndex == -1 ? normalizedPath : normalizedPath.substring(lastIndex + 1);

            final Set<File> files = new HashSet<>();
            final Set<File> resources = Resources.getResourcesAsFiles(path);
            for (final File parent : resources) {
                final File[] filteredResources = parent.listFiles((dir, file) -> file.startsWith(fileName));
                if (filteredResources != null)
                    files.addAll(Arrays.asList(filteredResources));
            }
            return files.stream().map(File::toPath).collect(Collectors.toUnmodifiableSet());
        }
        catch (final NullPointerException | IOException e) {
            return new HashSet<>();
        }
    }

    @Override
    public URI classpathUri() {
        try {
            return Hartshorn.class.getClassLoader().getResource("").toURI();
        }
        catch (final URISyntaxException e) {
            this.applicationContext.handle("Could not look up classpath base", e);
            return null;
        }
    }
}