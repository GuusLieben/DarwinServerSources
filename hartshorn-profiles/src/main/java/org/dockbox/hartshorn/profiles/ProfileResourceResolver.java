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

import java.net.URI;
import java.util.Set;

/**
 * Resolver to obtain resources for a specific profile. This can be used to load configuration files, for example.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
@FunctionalInterface
public interface ProfileResourceResolver {

    /**
     * Resolves the resources for the given profile name.
     *
     * @param profileName the name of the profile
     * @return the resources for the profile
     */
    Set<URI> resolve(String profileName);
}
