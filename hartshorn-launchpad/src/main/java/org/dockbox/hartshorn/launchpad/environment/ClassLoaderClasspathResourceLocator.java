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

package org.dockbox.hartshorn.launchpad.environment;

import org.dockbox.hartshorn.launchpad.ApplicationContext;
import org.dockbox.hartshorn.util.option.Option;
import org.dockbox.hartshorn.launchpad.resources.Resources;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The default implementation of {@link ClasspathResourceLocator}. This implementation will copy the resource to a temporary
 * location and return the path to the temporary location.
 *
 * @since 0.4.9
 *
 * @author Guus Lieben
 */
public class ClassLoaderClasspathResourceLocator implements ClasspathResourceLocator {

    private final ApplicationEnvironment environment;

    public ClassLoaderClasspathResourceLocator(ApplicationEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public Option<Path> resource(String name) throws IOException {
        File file = Resources.getResourceAsFile(name);
        if(file.exists()) {
            return Option.of(file.toPath());
        }
        return Option.empty();
    }

    @Override
    public Set<Path> resources(String name) {
        String normalizedPath = name.replaceAll("\\\\", "/");
        int lastIndex = normalizedPath.lastIndexOf("/");
        String path = lastIndex == -1 ? "" : normalizedPath.substring(0, lastIndex + 1);
        String fileName = lastIndex == -1 ? normalizedPath : normalizedPath.substring(lastIndex + 1);

        Set<File> files = new HashSet<>();
        Set<File> resources;

        try {
            resources = Resources.getResourcesAsFiles(path);
        }
        catch (NullPointerException | IOException e) {
            return new HashSet<>();
        }

        for (File parent : resources) {
            File[] filteredResources = parent.listFiles((dir, file) -> file.startsWith(fileName));
            if(filteredResources != null) {
                files.addAll(Arrays.asList(filteredResources));
            }
        }

        return files.stream().map(File::toPath).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public URI classpathUri() {
        try {
            URL resource = ApplicationContext.class.getClassLoader().getResource("");
            if (resource == null) {
                return null;
            }
            return resource.toURI();
        }
        catch (URISyntaxException e) {
            this.environment.handle("Could not look up classpath base", e);
            return null;
        }
    }
}
