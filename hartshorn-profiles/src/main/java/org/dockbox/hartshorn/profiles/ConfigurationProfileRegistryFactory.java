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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dockbox.hartshorn.properties.PropertyRegistry;
import org.dockbox.hartshorn.properties.loader.PropertyRegistryLoader;
import org.dockbox.hartshorn.properties.value.StandardPropertyParsers;
import org.dockbox.hartshorn.util.ApplicationRuntimeException;

public class ConfigurationProfileRegistryFactory implements ProfileRegistryFactory {

    private final PropertyRegistryLoader propertyRegistryLoader;
    private final ProfileResourceResolver resourceResolver;
    private final PropertyRegistrySupplier registrySupplier;

    public ConfigurationProfileRegistryFactory(
            PropertyRegistryLoader propertyRegistryLoader,
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

        SimpleEnvironmentProfile defaultProfile = new SimpleEnvironmentProfile("default", rootRegistry);
        profileRegistry.register(0, defaultProfile);

        // TODO: Complex parser using predicate matching
        List<EnvironmentProfile> additionalProfiles = rootRegistry.value("hartshorn.profiles", StandardPropertyParsers.STRING_LIST)
                .map(this::profiles)
                .orElseGet(List::of);

        for(int i = 0; i < additionalProfiles.size(); i++) {
            EnvironmentProfile profile = additionalProfiles.get(i);
            profileRegistry.register(i + 1, profile);
        }

        return profileRegistry;
    }

    protected List<EnvironmentProfile> profiles(String[] profileNames) {
        List<EnvironmentProfile> profiles = new ArrayList<>();
        for(String profileName : profileNames) {
            Set<URI> resources = resourceResolver.resolve(profileName);
            PropertyRegistry registry = registrySupplier.get();
            for(URI resource : resources) {
                try {
                    propertyRegistryLoader.loadRegistry(registry, Path.of(resource));
                }
                catch(IOException e) {
                    // TODO: Better exception type
                    throw new ApplicationRuntimeException("Failed to load profile " + profileName, e);
                }
            }
            profiles.add(new SimpleEnvironmentProfile(profileName, registry));
        }
        return profiles;
    }
}
