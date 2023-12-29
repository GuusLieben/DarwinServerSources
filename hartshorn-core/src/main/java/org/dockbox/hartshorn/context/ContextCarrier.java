/*
 * Copyright 2019-2023 the original author or authors.
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

package org.dockbox.hartshorn.context;

import org.dockbox.hartshorn.application.context.ApplicationContext;

/**
 * A context carrier is a class that can be used to transport an active {@link ApplicationContext}.
 *
 * @since 0.4.4
 *
 * @author Guus Lieben
 */
@FunctionalInterface
public interface ContextCarrier {

    /**
     * Gets the active {@link ApplicationContext}.
     *
     * @return The active {@link ApplicationContext}.
     */
    ApplicationContext applicationContext();
}
