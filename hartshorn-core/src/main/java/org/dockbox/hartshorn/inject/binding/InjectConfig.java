/*
 * Copyright 2019-2022 the original author or authors.
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

package org.dockbox.hartshorn.inject.binding;

import org.dockbox.hartshorn.application.Activator;

/**
 * Used by {@link Activator} to add default
 * {@link InjectConfiguration}s to the application instance.
 *
 * @author Guus Lieben
 * @since 21.2
 */
public @interface InjectConfig {

    /**
     * The {@link InjectConfiguration} to add to the application instance.
     * @return The {@link InjectConfiguration} to add to the application instance.
     */
    Class<? extends InjectConfiguration> value();
}