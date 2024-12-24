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

import java.net.URI;
import java.util.Set;

/**
 * Defines how a resource is looked up when using {@link ResourceLookup}. It remains up to the {@link ResourceLookup}
 * to decide which strategy to use for a given resource path, though it is recommended to use the strategy name as a
 * prefix to the resource path, e.g. {@code classpath:my/resource/path}.
 *
 * @see ResourceLookup
 *
 * @since 0.4.7
 *
 * @author Guus Lieben
 */
public interface ResourceLookupStrategy {

    /**
     * Returns the name of the strategy. This name is used to identify the strategy when using {@link ResourceLookup}.
     * It is recommended to use a prefix that is unique to the strategy, e.g. {@code classpath}. This allows for a
     * {@link ResourceLookup} to use multiple strategies, and still be able to identify which strategy to use for a
     * given resource path.
     *
     * @return the name of the strategy
     */
    String name();

    /**
     * Looks up all compatible resources for the given path. The returned {@link URI URIs} are expected to be absolute.
     *
     * @param environment the application environment
     * @param path the path to the resource
     * @return a set of {@link URI URIs} pointing to the resource
     */
    Set<URI> lookup(ApplicationEnvironment environment, String path);

    /**
     * Returns the base URL for this strategy, from where all relative resources are resolved. This URL is expected to
     * be absolute.
     *
     * @param environment the application environment
     * @return the base URL for this strategy
     */
    URI baseUrl(ApplicationEnvironment environment);
}
