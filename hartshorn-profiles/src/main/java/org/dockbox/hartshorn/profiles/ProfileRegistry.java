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

import java.util.List;

/**
 * A registry for {@link EnvironmentProfile} instances. Profiles are used to group configuration properties and
 * resources together, and can be used to create a hierarchy of configuration properties.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public interface ProfileRegistry {

    /**
     * Retrieves a profile by its name. If no profile with the given name is found, an empty {@link Option} is returned.
     *
     * @param name the name of the profile to retrieve
     * @return the profile, if found
     */
    Option<EnvironmentProfile> profile(String name);

    /**
     * Registers a new profile with the given priority. The profile will be added to the registry, and will be used
     * to resolve configuration properties. The priority determines the order in which profiles are resolved. Lower
     * priority values are resolved first.
     *
     * @param priority the priority of the profile
     * @param profile the profile to register
     */
    void register(int priority, EnvironmentProfile profile);

    /**
     * Unregisters a profile from the registry. The profile will no longer be used to resolve configuration properties.
     *
     * @param profile the profile to unregister
     */
    void unregister(EnvironmentProfile profile);

    /**
     * Retrieves all profiles in the registry, ordered by priority. The profile with the lowest priority is first in
     * the list.
     *
     * @return all profiles in the registry
     */
    List<EnvironmentProfile> profiles();
}
