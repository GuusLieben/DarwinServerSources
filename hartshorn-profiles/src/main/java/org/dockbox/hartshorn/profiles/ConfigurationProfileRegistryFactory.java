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

package org.dockbox.hartshorn.profiles;

import org.dockbox.hartshorn.properties.PropertyRegistry;
import org.dockbox.hartshorn.properties.loader.PropertyRegistryPathLoader;
import org.dockbox.hartshorn.util.ApplicationRuntimeException;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Standard implementation of {@link ProfileRegistryFactory} that creates a {@link ProfileRegistry} based on
 * a root {@link PropertyRegistry} and additional profiles defined in the root registry. Additional profiles
 * should be defined in a property list with the key {@value #PROFILES_PROPERTY}.
 *
 * <p>For example, if the root registry contains a property list with the key {@value #PROFILES_PROPERTY} and
 * the value {@code ["profile1", "profile2"]}, this factory will create a registry with three profiles:
 * {@value #DEFAULT_PROFILE_NAME}, {@code profile1} and {@code profile2}.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class ConfigurationProfileRegistryFactory implements ProfileRegistryFactory {

    private static final String PROFILES_PROPERTY = "hartshorn.profiles";
    private static final String DEFAULT_PROFILE_NAME = "default";

    private final PropertyRegistryPathLoader propertyRegistryLoader;
    private final ProfileResourceResolver resourceResolver;
    private final PropertyRegistrySupplier registrySupplier;

    public ConfigurationProfileRegistryFactory(
            PropertyRegistryPathLoader propertyRegistryLoader,
            ProfileResourceResolver resourceResolver,
            PropertyRegistrySupplier registrySupplier
    ) {
        this.propertyRegistryLoader = propertyRegistryLoader;
        this.resourceResolver = resourceResolver;
        this.registrySupplier = registrySupplier;
    }

    @Override
    public ProfileRegistry create(PropertyRegistry rootRegistry) {
        ProfileRegistry profileRegistry = new ConcurrentProfileRegistry();

        EnvironmentProfile defaultProfile = new SimpleEnvironmentProfile(DEFAULT_PROFILE_NAME, rootRegistry);
        profileRegistry.register(0, defaultProfile);

        List<EnvironmentProfile> additionalProfiles = rootRegistry.list(PROFILES_PROPERTY)
                .stream(list -> list.values().stream())
                .flatMap(property -> property.value().stream())
                .map(this::resolveProfile)
                .toList();

        for(int i = 0; i < additionalProfiles.size(); i++) {
            EnvironmentProfile profile = additionalProfiles.get(i);
            profileRegistry.register(i + 1, profile);
        }

        return profileRegistry;
    }

    private EnvironmentProfile resolveProfile(String profileName) {
        Set<URI> resources = this.resourceResolver.resolve(profileName);
        PropertyRegistry registry = this.registrySupplier.get();
        for(URI resource : resources) {
            try {
                this.propertyRegistryLoader.loadRegistry(registry, Path.of(resource));
            }
            catch(IOException e) {
                // TODO: Better exception type
                throw new ApplicationRuntimeException("Failed to load profile " + profileName, e);
            }
        }
        return new SimpleEnvironmentProfile(profileName, registry);
    }
}
