package org.dockbox.hartshorn.profiles;

import org.dockbox.hartshorn.properties.PropertyRegistry;

public record SimpleEnvironmentProfile(
        String name,
        PropertyRegistry propertyRegistry
) implements EnvironmentProfile { }
