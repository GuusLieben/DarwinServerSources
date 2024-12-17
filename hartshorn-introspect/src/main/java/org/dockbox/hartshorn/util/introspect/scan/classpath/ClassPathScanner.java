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

package org.dockbox.hartshorn.util.introspect.scan.classpath;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A classpath scanner that can be used to scan the classpath for resources. This scanner is capable of scanning both
 * directories and jar files. The scanner can be configured to scan for classes, resources, or both. The scanner can
 * also be configured to exclude inner classes and package-info classes.
 *
 * <p>The scanner can be configured to scan the classpath in a number of ways:
 * <ul>
 *     <li>By adding a {@link ClassLoader} to the scanner</li>
 *     <li>By adding a {@link URL} to the scanner</li>
 *     <li>By adding a system property that contains a classpath entry</li>
 *     <li>By including the default classpath</li>
 * </ul>
 *
 * <p>When the scanner is configured, it can be used to scan the classpath. The scanner will delegate the processing of
 * each file to a given {@link ResourceHandler}. The scanner will only process files that are compatible with the
 * configured scan settings.
 *
 * <p>Typically, {@link ClassPathScanner} should not be used directly, but rather be used through the {@link
 * ClassPathScannerTypeReferenceCollector}.
 *
 * @since 0.4.13
 *
 * @author Guus Lieben
 */
public final class ClassPathScanner {

    private final Set<String> classNames = new HashSet<>();
    private final Set<URLClassLoader> classLoaders = new HashSet<>();
    private final Set<String> prefixFilters = new HashSet<>();

    private boolean resourcesOnly = false;
    private boolean classesOnly = true;
    private boolean excludeInnerClasses = false;
    private boolean excludePackageInfo = true;
    private long scanTime = -1;

    private ClassPathScanner() {
        // Private constructor to prevent instantiation outside of #create()
    }

    /**
     * Creates a new {@link ClassPathScanner} instance. The returned instance is configured to scan for classes only,
     * and exclude package-info.
     *
     * @return The created instance
     */
    public static ClassPathScanner create() {
        return new ClassPathScanner();
    }

    /**
     * Adds the value of a system property to the scanner. The value of the system property is expected to be a
     * classpath entry. The value of the system property is split on the {@link File#pathSeparatorChar} character.
     * Each resulting path is added to the scanner if it is a valid path.
     *
     * @param key The name of the system property
     * @return The scanner instance
     */
    public synchronized ClassPathScanner addSystemPropertyPaths(String key) {
        String value = System.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return this;
        }

        for (String path : value.split(String.valueOf(File.pathSeparatorChar), -1)) {
            if (path == null || path.trim().isEmpty()) {
                continue;
            }
            File file = new File(path);
            if (!file.exists()) {
                continue;
            }

            try {
                URL url = file.toURI().toURL();
                this.addUrlForScanning(url);
            }
            catch (MalformedURLException e) {
                // Ignore
            }
        }
        return this;
    }

    /**
     * Adds a {@link URL} to the scanner. The URL is expected to be a classpath entry. The URL is added to the scanner
     * if it is a valid path.
     *
     * @param url The URL to add
     * @return The scanner instance
     */
    public synchronized ClassPathScanner addUrlForScanning(URL url) {
        return this.addClassLoaderForScanning(new URLClassLoader(new URL[] { url }) {
            @Override
            public String toString() {
                return super.toString() + " [url=" + url.toExternalForm() + "]";
            }
        });
    }

    /**
     * Adds a {@link URLClassLoader} to the scanner.
     *
     * @param classLoader The classloader to add
     * @return The scanner instance
     */
    public synchronized ClassPathScanner addClassLoaderForScanning(URLClassLoader classLoader) {
        this.classLoaders.add(classLoader);
        return this;
    }

    /**
     * Scans the classpath for resources. The scanner will delegate the processing of each file to the provided {@link
     * ResourceHandler}. The scanner will only process files that are compatible with the configured scan settings.
     *
     * @param handler The handler that will consume the file if it is compatible
     * @return The scanner instance
     * @throws ClassPathWalkingException When an error occurs while scanning the classpath
     */
    public synchronized ClassPathScanner scan(ResourceHandler handler) throws ClassPathWalkingException {
        this.classNames.clear();

        long start = System.currentTimeMillis();
        for (URLClassLoader classLoader : this.classLoaders) {
            this.scanClassLoaderResources(handler, classLoader);
        }

        this.scanTime = System.currentTimeMillis() - start;
        return this;
    }

    /**
     * Scans the given {@link URLClassLoader} for resources. The scanner will delegate the processing of each file to
     * the provided {@link ResourceHandler}. The scanner will only process files that are compatible with the configured
     * scan settings.
     *
     * @param handler The handler that will consume the file if it is compatible
     * @param classLoader The classloader to scan
     * @throws ClassPathWalkingException When an error occurs while scanning the classpath
     */
    private void scanClassLoaderResources(ResourceHandler handler, URLClassLoader classLoader) throws ClassPathWalkingException {
        for (URL url : classLoader.getURLs()) {
            if (url.getFile() != null && !url.getFile().isEmpty()) {

                // Physical files can have escaped characters in the URL representation. The simplest form of this is
                // %20 instead of a space. This is not valid in a URI, so we need to decode the URL to get the correct
                // file path.
                String decodedUrl = URLDecoder.decode(url.getFile(), Charset.defaultCharset());
                File file = new File(decodedUrl);
                if (file.exists()) {
                    if (file.isDirectory()) {
                        this.processDirectoryResource(handler, classLoader, file);
                    }
                    else if (file.isFile() && file.getName().toLowerCase(Locale.ROOT).endsWith(".jar")) {
                        this.processJarFileResource(handler, classLoader, file);
                    }
                }
                else {
                    throw new ClassPathWalkingException("Unsupported classpath resource: " + url);
                }
            }
        }
    }

    /**
     * Processes a jar file resource. This will only process files that are compatible with the configured scan
     * settings. Any file that is compatible will be delegated to the provided {@link ResourceHandler}.
     *
     * @param handler The handler that will consume the file if it is compatible
     * @param classLoader The classloader to use for loading classes from the jar file
     * @param jarFile The jar file
     */
    private void processJarFileResource(ResourceHandler handler, URLClassLoader classLoader, File jarFile) {
        try(JarFile file = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = file.entries();
            while(entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if(!entry.isDirectory()) {
                    String name = entry.getName();
                    this.processPathResource(handler, classLoader, name, jarFile.toPath());
                }
            }
        }
        catch(IOException e) {
            // Handle exception
        }
    }

    /**
     * Processes a directory resource. This delegates the file visiting to a {@link DirectoryFileTreeWalker}. The
     * walker will delegate the processing of each file to the provided {@link ResourceHandler}. The scanner will only
     * process files that are compatible with the configured scan settings.
     *
     * @param handler The handler that will consume the file if it is compatible
     * @param classLoader The classloader to use for loading classes from the jar file
     * @param directory The directory to scan
     * @throws ClassPathWalkingException When an error occurs while scanning the classpath
     */
    private void processDirectoryResource(ResourceHandler handler, URLClassLoader classLoader, File directory) throws ClassPathWalkingException {
        try {
            File rootDir = directory.getCanonicalFile();
            int rootDirNameLen = rootDir.getCanonicalPath().length();
            Files.walkFileTree(rootDir.toPath(), new DirectoryFileTreeWalker(this, rootDirNameLen, handler, classLoader));
        }
        catch (IOException e) {
            throw new ClassPathWalkingException("Could not process directory resource " + directory.getPath(), e);
        }
    }

    /**
     * Processes a path resource. The scanner will only process files that are compatible with the configured scan
     * settings. Any file that is compatible will be delegated to the provided {@link ResourceHandler}.
     *
     * @param handler The handler that will consume the file if it is compatible
     * @param classLoader The classloader to use for loading classes from the jar file
     * @param resourceName The name of the resource
     * @param path The path to the resource
     */
    void processPathResource(ResourceHandler handler, URLClassLoader classLoader, String resourceName, Path path) {
        // If there's nowhere to delegate the resource to, don't process it
        if (handler == null) {
            return;
        }

        boolean isClassResource = resourceName.toLowerCase(Locale.ROOT).endsWith(".class");
        String checkedResourceName = !isClassResource
                ? resourceName
                : this.resourceToCanonicalName(resourceName);

        if (!this.shouldProcessResource(isClassResource, checkedResourceName)) {
            return;
        }

        ClassPathResource resource = new ClassCandidateResource(classLoader, path, checkedResourceName, isClassResource);
        handler.handle(resource);
    }

    /**
     * Determines if a resource should be processed by the scanner. The scanner will only process files that are
     * compatible with the configured scan settings.
     *
     * @param isClassResource Whether the resource is a class
     * @param checkedResourceName The name of the resource. This should be the canonical name of the class if the
     *                            resource is a class
     *
     * @return True if the resource should be processed, false otherwise
     */
    private boolean shouldProcessResource(boolean isClassResource, String checkedResourceName) {
        // If we're filtering by prefix, and the resource name doesn't start with any of the prefixes, don't process it
        for (String beginFilterName : this.prefixFilters) {
            if (!checkedResourceName.startsWith(beginFilterName)) {
                return false;
            }
        }

        // If we're scanning for classes, and the resource is a class that was previously scanned, don't
        // process it again
        if (isClassResource && this.classesOnly && this.classNames.contains(checkedResourceName)) {
            return false;
        }

        // If we're scanning for classes, don't process any non-class resources
        if (this.classesOnly && !isClassResource) {
            return false;
        }

        // If we're scanning for resources, don't process any class resources
        if (this.resourcesOnly && isClassResource) {
            return false;
        }

        // Skip inner classes
        if (this.excludeInnerClasses && isClassResource && checkedResourceName.indexOf('$') > -1) {
            return false;
        }

        // Skip package-info classes
        return !this.excludePackageInfo || !isClassResource || !checkedResourceName.endsWith("package-info");

        // If none of the above conditions are met, we should process the resource
    }

    @NonNull
    private String resourceToCanonicalName(String resourceName) {
        return resourceName.substring(0, resourceName.length() - 6)
                .replace('/', '.')
                .replace('\\', '.');
    }

    /**
     * Adds a prefix filter to the scanner. The scanner will only process files that start with any of the provided
     * prefixes.
     *
     * @param prefix The prefix to add
     * @return The scanner instance
     */
    public synchronized ClassPathScanner filterPrefix(String prefix) {
        if (prefix != null) {
            this.prefixFilters.add(prefix);
        }
        return this;
    }

    /**
     * Returns the set of prefixes that are configured in the scanner. When scanning the classpath, the scanner will
     * only process files that start with any of these prefixes.
     *
     * @return The set of prefixes
     */
    public synchronized Set<String> filteredPrefixes() {
        return this.prefixFilters;
    }

    /**
     * Includes the default classpath in the scanner. The default classpath is determined by the value of the {@code
     * java.class.path} system property.
     *
     * @return The scanner instance
     */
    public synchronized ClassPathScanner includeDefaultClassPath() {
        return this.addSystemPropertyPaths("java.class.path");
    }

    /**
     * Configures the scanner to scan for classes only. This is the default setting.
     *
     * @return The scanner instance
     */
    public synchronized ClassPathScanner classesOnly() {
        this.classesOnly = true;
        this.resourcesOnly = false;
        return this;
    }

    /**
     * Configures the scanner to scan for resources only.
     *
     * @return The scanner instance
     */
    public synchronized ClassPathScanner resourcesOnly() {
        this.classesOnly = false;
        this.resourcesOnly = true;
        return this;
    }

    /**
     * Configures the scanner to exclude inner classes. This assumes that classes are otherwise included in
     * the scan configuration.
     *
     * @return The scanner instance
     */
    public synchronized ClassPathScanner excludeInnerClasses() {
        this.excludeInnerClasses = true;
        return this;
    }

    /**
     * Configures the scanner to include package-info classes. This assumes that classes are otherwise included in
     * the scan configuration.
     *
     * @return The scanner instance
     */
    public synchronized ClassPathScanner includePackageInfo() {
        this.excludePackageInfo = false;
        return this;
    }

    /**
     * Returns the set of {@link ClassLoader}s that are configured in the scanner. When scanning the classpath, the
     * scanner will use these classloaders to discover resources.
     *
     * @return The set of classloaders
     */
    public synchronized Set<ClassLoader> classLoaders() {
        return Collections.unmodifiableSet(this.classLoaders);
    }

    /**
     * Returns the set of prefixes that are configured in the scanner. When scanning the classpath, the scanner will
     * only process files that start with any of these prefixes.
     *
     * @return The set of prefixes
     */
    public synchronized long scanTime() {
        return this.scanTime;
    }
}
