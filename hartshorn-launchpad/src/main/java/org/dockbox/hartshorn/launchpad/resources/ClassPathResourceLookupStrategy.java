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

package org.dockbox.hartshorn.launchpad.resources;

import org.dockbox.hartshorn.launchpad.environment.ApplicationEnvironment;
import org.dockbox.hartshorn.launchpad.environment.ClasspathResourceLocator;

import java.net.URI;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Looks up a resource through the classpath. The packaged resource is copied to a temporary file, created and managed
 * by {@link ClasspathResourceLocator#resources(String)}. This requires the strategy name to be
 * configured to be equal to {@code classpath:{resource_name}}.
 *
 * @since 0.4.7
 *
 * @author Guus Lieben
 */
public class ClassPathResourceLookupStrategy implements ResourceLookupStrategy {

    public static final String NAME = "classpath";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Set<URI> lookup(ApplicationEnvironment environment, String path) {
        return environment.classpath().resources(path).stream().map(Path::toUri).collect(Collectors.toSet());
    }

    @Override
    public URI baseUrl(ApplicationEnvironment environment) {
        return environment.classpath().classpathUri();
    }
}
