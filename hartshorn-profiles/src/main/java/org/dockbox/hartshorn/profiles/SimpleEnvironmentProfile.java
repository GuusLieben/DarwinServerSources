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

import org.dockbox.hartshorn.properties.PropertyRegistry;

/**
 * A simple implementation of {@link EnvironmentProfile}.
 *
 * @param name the name of the profile
 * @param propertyRegistry the property registry of the profile
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public record SimpleEnvironmentProfile(
        String name,
        PropertyRegistry propertyRegistry
) implements EnvironmentProfile { }
