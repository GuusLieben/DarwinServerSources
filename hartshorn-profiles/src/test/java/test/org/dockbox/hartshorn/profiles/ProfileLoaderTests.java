package test.org.dockbox.hartshorn.profiles;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Set;

import org.dockbox.hartshorn.profiles.ConfigurationProfileRegistryFactory;
import org.dockbox.hartshorn.properties.MapPropertyRegistry;
import org.dockbox.hartshorn.properties.loader.support.CompositePredicatePropertyRegistryLoader;
import org.dockbox.hartshorn.properties.loader.support.JacksonJavaPropsPropertyRegistryLoader;
import org.dockbox.hartshorn.properties.loader.support.JacksonYamlPropertyRegistryLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProfileLoaderTests {

    @Test
    void testLoadProfilesInOrder() throws IOException {
        var propertyRegistryLoader = new CompositePredicatePropertyRegistryLoader();
        propertyRegistryLoader.addLoader(new JacksonYamlPropertyRegistryLoader());
        propertyRegistryLoader.addLoader(new JacksonJavaPropsPropertyRegistryLoader());

        var profileRegistryFactory = new ConfigurationProfileRegistryFactory(propertyRegistryLoader, name -> {
            return Set.of(
                    new File("C:\\Projects\\Temp\\Hartshorn\\hartshorn-profiles\\src\\test\\resources\\application-%s.yml".formatted(name)).toURI()
            );
        }, MapPropertyRegistry::new);

        var rootRegistry = new MapPropertyRegistry();
        propertyRegistryLoader.loadRegistry(rootRegistry, Path.of("C:\\Projects\\Temp\\Hartshorn\\hartshorn-profiles\\src\\test\\resources\\application.yml"));
        var profileRegistry = profileRegistryFactory.create(rootRegistry);
        var profilesInOrder = profileRegistry.profiles();
        Assertions.assertEquals(3, profilesInOrder.size());
        Assertions.assertEquals("default", profilesInOrder.get(0).name());
        Assertions.assertEquals("base", profilesInOrder.get(1).name());
        Assertions.assertEquals("dev", profilesInOrder.get(2).name());
    }
}
