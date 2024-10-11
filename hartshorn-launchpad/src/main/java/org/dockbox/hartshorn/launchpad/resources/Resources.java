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

import org.dockbox.hartshorn.launchpad.ApplicationContext;
import org.dockbox.hartshorn.launchpad.environment.ClasspathResourceLocator;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utilities for working with classpath resources. This class is internal, and should not be used directly. Instead,
 * refer to a {@link ClasspathResourceLocator}.
 *
 * @see ClasspathResourceLocator
 *
 * @since 0.4.13
 *
 * @author Guus Lieben
 */
public final class Resources {

    private Resources() {
    }

    /**
     * Returns a {@link URL} pointing to the given resource. This method will first attempt to find the resource
     * using the {@link Thread#getContextClassLoader() current thread's context class loader} , and if that fails,
     * will attempt to find the resource using the system classloader.
     *
     * @param resource the name of the resource
     * @return a {@link URL} pointing to the given resource
     * @throws IOException if the resource could not be found
     */
    public static URL getResourceURL(String resource) throws IOException {
        return getResourceURL(getClassLoader(), resource);
    }

    /**
     * Returns a {@link URL} pointing to the given resource. This method will first attempt to find the resource
     * using the provided {@link ClassLoader}, and if that fails, will attempt to find the resource using the
     * system classloader.
     *
     * @param loader the {@link ClassLoader} to use to find the resource
     * @param resource the name of the resource
     * @return a {@link URL} pointing to the given resource
     * @throws IOException if the resource could not be found
     */
    public static URL getResourceURL(ClassLoader loader, String resource) throws IOException {
        URL url = null;
        if (loader != null) {
            url = loader.getResource(resource);
        }
        if (url == null) {
            url = ClassLoader.getSystemResource(resource);
        }
        if (url == null) {
            throw new IOException("Could not find resource " + resource);
        }
        return url;
    }

    /**
     * Returns a {@link File} pointing to the given resource. This method will first attempt to find the resource
     * using the {@link Thread#getContextClassLoader() current thread's context class loader} , and if that fails,
     * will attempt to find the resource using the system classloader.
     *
     * @param resource the name of the resource
     * @return a {@link File} pointing to the given resource
     * @throws IOException if the resource could not be found
     */
    public static File getResourceAsFile(String resource) throws IOException {
        return new File(getResourceURL(resource).getFile());
    }

    /**
     * Returns a {@link File} pointing to the given resource. This method will first attempt to find the resource
     * using the provided {@link ClassLoader}, and if that fails, will attempt to find the resource using the
     * system classloader.
     *
     * @param loader the {@link ClassLoader} to use to find the resource
     * @param resource the name of the resource
     * @return a {@link File} pointing to the given resource
     * @throws IOException if the resource could not be found
     */
    public static File getResourceAsFile(ClassLoader loader, String resource) throws IOException {
        return new File(getResourceURL(loader, resource).getFile());
    }

    /**
     * Returns a {@link Set} of {@link URL}s pointing to all resources with the given name. This method will first
     * attempt to find the resource using the {@link Thread#getContextClassLoader() current thread's context class
     * loader} , and if that fails, will attempt to find the resource using the system classloader.
     *
     * @param resource the name of the resource
     * @return a {@link Set} of {@link URL}s pointing to all resources with the given name
     * @throws IOException if the resource could not be found
     */
    public static Set<URL> getResourceURLs(String resource) throws IOException {
        return getResourceURLs(getClassLoader(), resource);
    }

    /**
     * Returns a {@link Set} of {@link URL}s pointing to all resources with the given name. This method will first
     * attempt to find the resource using the provided {@link ClassLoader}, and if that fails, will attempt to find
     * the resource using the system classloader.
     *
     * @param loader the {@link ClassLoader} to use to find the resource
     * @param resource the name of the resource
     * @return a {@link Set} of {@link URL}s pointing to all resources with the given name
     * @throws IOException if the resource could not be found
     */
    @SuppressWarnings("UrlHashCode")
    public static Set<URL> getResourceURLs(ClassLoader loader, String resource) throws IOException {
        Set<URL> urls = new HashSet<>();
        if (loader != null) {
            Enumeration<URL> resources = loader.getResources(resource);
            if (resources != null) {
                while (resources.hasMoreElements()) {
                    urls.add(resources.nextElement());
                }
            }
        }
        Enumeration<URL> systemResources = ClassLoader.getSystemResources(resource);
        if (systemResources != null) {
            while (systemResources.hasMoreElements()) {
                urls.add(systemResources.nextElement());
            }
        }
        return Collections.unmodifiableSet(urls);
    }

    /**
     * Returns a {@link Set} of {@link File}s pointing to all resources with the given name. This method will first
     * attempt to find the resource using the {@link Thread#getContextClassLoader() current thread's context class
     * loader} , and if that fails, will attempt to find the resource using the system classloader.
     *
     * @param resource the name of the resource
     * @return a {@link Set} of {@link File}s pointing to all resources with the given name
     * @throws IOException if the resource could not be found
     */
    public static Set<File> getResourcesAsFiles(String resource) throws IOException {
        return getResourcesAsFiles(getClassLoader(), resource);
    }

    /**
     * Returns a {@link Set} of {@link File}s pointing to all resources with the given name. This method will first
     * attempt to find the resource using the provided {@link ClassLoader}, and if that fails, will attempt to find
     * the resource using the system classloader.
     *
     * @param loader the {@link ClassLoader} to use to find the resource
     * @param resource the name of the resource
     * @return a {@link Set} of {@link File}s pointing to all resources with the given name
     * @throws IOException if the resource could not be found
     */
    public static Set<File> getResourcesAsFiles(ClassLoader loader, String resource) throws IOException {
        return getResourceURLs(loader, resource).stream()
                .map(url -> new File(url.getFile()))
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns a {@link Set} of {@link URI}s pointing to all resources with the given name. This method will request
     * each provided {@link ResourceLookupStrategy} to find the resource, and will return the union of all results.
     *
     * @param applicationContext the {@link ApplicationContext} to use to find the resource
     * @param resource the name of the resource
     * @param strategies the {@link ResourceLookupStrategy}s to use to find the resource
     * @return a {@link Set} of {@link URI}s pointing to all resources with the given name
     */
    public static Set<URI> getResourceURIs(ApplicationContext applicationContext, String resource, ResourceLookupStrategy... strategies) {
        Set<URI> uris = new HashSet<>();
        for (ResourceLookupStrategy strategy : strategies) {
            uris.addAll(strategy.lookup(applicationContext, resource));
        }
        return Collections.unmodifiableSet(uris);
    }

    /**
     * Returns the {@link ClassLoader} to use to find resources. This method will first attempt to find the
     * {@link Thread#getContextClassLoader() current thread's context class loader} , and if that fails, will
     * return the current class' {@link ClassLoader}.
     *
     * @return the {@link ClassLoader} to use to find resources
     */
    private static ClassLoader getClassLoader() {
        try {
            return Thread.currentThread().getContextClassLoader();
        } catch (SecurityException e) {
            return ApplicationContext.class.getClassLoader();
        }
    }
}
