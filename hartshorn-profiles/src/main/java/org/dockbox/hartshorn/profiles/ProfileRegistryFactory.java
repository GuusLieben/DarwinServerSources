package org.dockbox.hartshorn.profiles;

import org.dockbox.hartshorn.properties.PropertyRegistry;

@FunctionalInterface
public interface ProfileRegistryFactory {

    ProfileRegistry create(PropertyRegistry rootRegistry);

}
