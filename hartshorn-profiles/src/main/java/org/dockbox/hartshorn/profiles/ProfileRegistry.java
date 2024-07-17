package org.dockbox.hartshorn.profiles;

import java.util.List;
import java.util.Set;

import org.dockbox.hartshorn.util.option.Option;

public interface ProfileRegistry {

    Option<EnvironmentProfile> profile(String name);

    // Lower priority is higher precedence (e.g. 0 goes before 1)
    void register(int priority, EnvironmentProfile profile);

    void unregister(EnvironmentProfile profile);

    // In order of priority
    List<EnvironmentProfile> profiles();
}
