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

import org.dockbox.hartshorn.util.option.Option;
import org.dockbox.hartshorn.util.stream.EntryStream;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A concurrent implementation of the {@link ProfileRegistry} interface.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
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
        return EntryStream.of(this.prioritizedProfiles)
                .sortedKeys()
                .values()
                .toList();
    }
}
