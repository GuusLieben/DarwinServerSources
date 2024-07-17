package org.dockbox.hartshorn.profiles;

import org.dockbox.hartshorn.properties.PropertyRegistry;

public interface EnvironmentProfile {

    String name();

    PropertyRegistry propertyRegistry();
}
