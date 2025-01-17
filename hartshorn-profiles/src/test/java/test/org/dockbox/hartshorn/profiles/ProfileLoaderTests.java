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

package test.org.dockbox.hartshorn.profiles;

import org.dockbox.hartshorn.profiles.ConfigurationProfileRegistryFactory;
import org.dockbox.hartshorn.profiles.ProfileRegistryFactory;
import org.dockbox.hartshorn.properties.MapPropertyRegistry;
import org.dockbox.hartshorn.properties.PropertyRegistry;
import org.dockbox.hartshorn.properties.loader.support.CompositePredicatePropertyRegistryLoader;
import org.dockbox.hartshorn.properties.loader.support.JacksonJavaPropsPropertyRegistryLoader;
import org.dockbox.hartshorn.properties.loader.support.JacksonYamlPropertyRegistryLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class ProfileLoaderTests {

    @Test
    void testLoadProfilesInOrder() throws IOException {
        var propertyRegistryLoader = new CompositePredicatePropertyRegistryLoader();
        propertyRegistryLoader.addLoader(new JacksonYamlPropertyRegistryLoader());
        propertyRegistryLoader.addLoader(new JacksonJavaPropsPropertyRegistryLoader());

        ProfileRegistryFactory profileRegistryFactory = new ConfigurationProfileRegistryFactory(propertyRegistryLoader, name -> {
            return Set.of(
                    Path.of("src/test/resources/application-%s.yml".formatted(name)).toUri()
            );
        }, MapPropertyRegistry::new);

        PropertyRegistry rootRegistry = new MapPropertyRegistry();
        propertyRegistryLoader.loadRegistry(rootRegistry, Path.of("src/test/resources/application.yml"));
        var profileRegistry = profileRegistryFactory.create(rootRegistry);
        var profilesInOrder = profileRegistry.profiles();
        Assertions.assertEquals(3, profilesInOrder.size());
        Assertions.assertEquals("default", profilesInOrder.get(0).name());
        Assertions.assertEquals("base", profilesInOrder.get(1).name());
        Assertions.assertEquals("dev", profilesInOrder.get(2).name());
    }
}
