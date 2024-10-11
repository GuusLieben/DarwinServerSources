package org.dockbox.hartshorn.launchpad.launch;

import org.dockbox.hartshorn.launchpad.resources.Resources;
import org.dockbox.hartshorn.reporting.DiagnosticsPropertyCollector;
import org.dockbox.hartshorn.util.introspect.scan.ClassNameReference;
import org.dockbox.hartshorn.util.introspect.scan.TypeCollectionException;
import org.dockbox.hartshorn.util.introspect.scan.TypeReference;
import org.dockbox.hartshorn.util.introspect.scan.TypeReferenceCollector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ResourceConfigurationTypeReferenceCollector implements TypeReferenceCollector {

    @Override
    public Set<TypeReference> collect() throws TypeCollectionException {
        try {
            Set<File> resourceConfigurations = Resources.getResourcesAsFiles("META-INF/hartshorn.environment.types");
            Set<TypeReference> typeReferences = new HashSet<>();
            for (final File resourceConfiguration : resourceConfigurations) {
                typeReferences.addAll(collectConfigurationTypes(resourceConfiguration));
            }
            return typeReferences;
        }
        catch (final IOException e) {
            throw new TypeCollectionException("Could not access resource configurations", e);
        }
    }

    private List<TypeReference> collectConfigurationTypes(File resourceConfiguration) throws TypeCollectionException {
        try {
            List<String> configurationNames = Files.readAllLines(resourceConfiguration.toPath());
            return configurationNames.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(ClassNameReference::new)
                    .collect(Collectors.toUnmodifiableList());
        }
        catch (final IOException e) {
            throw new TypeCollectionException("Could not read configuration file", e);
        }
    }

    @Override
    public void report(DiagnosticsPropertyCollector collector) {

    }
}
