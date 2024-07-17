package org.dockbox.hartshorn.profiles;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dockbox.hartshorn.util.option.Option;

public class ConcurrentProfileRegistry implements ProfileRegistry {

    private final Map<Integer, EnvironmentProfile> prioritizedProfiles = new ConcurrentHashMap<>();

    @Override
    public Option<EnvironmentProfile> profile(String name) {
        return Option.of(this.prioritizedProfiles.values().stream()
                .filter(profile -> profile.name().equals(name))
                .findFirst());
    }

    @Override
    public void register(int priority, EnvironmentProfile profile) {
        if(this.prioritizedProfiles.containsKey(priority)) {
            throw new IllegalArgumentException("Profile with priority " + priority + " already exists");
        }
        if(this.prioritizedProfiles.values().stream().anyMatch(p -> p.name().equals(profile.name()))) {
            throw new IllegalArgumentException("Profile with name " + profile.name() + " already exists");
        }
        this.prioritizedProfiles.put(priority, profile);
    }

    @Override
    public void unregister(EnvironmentProfile profile) {
        this.prioritizedProfiles.values().removeIf(p -> p.name().equals(profile.name()));
    }

    @Override
    public List<EnvironmentProfile> profiles() {
        return this.prioritizedProfiles.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .toList();
    }
}
