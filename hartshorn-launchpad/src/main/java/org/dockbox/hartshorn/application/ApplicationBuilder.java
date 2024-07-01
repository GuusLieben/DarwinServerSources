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

package org.dockbox.hartshorn.application;

import org.dockbox.hartshorn.application.context.ApplicationContext;

/**
 * The {@link ApplicationBuilder} is responsible for creating the {@link ApplicationContext} and constructing the
 * required components. By default, all components are required, and will be validated before a {@link ApplicationContext}
 * is created.
 *
 * @param <C> The type of the {@link ApplicationContext} that is created.
 *
 * @since 0.4.8
 *
 * @author Guus Lieben
 */
@FunctionalInterface
public interface ApplicationBuilder<C extends ApplicationContext> {

    /**
     * Creates a new {@link ApplicationContext} instance with the components configured in this {@link ApplicationBuilder}.
     *
     * @return The created {@link ApplicationContext}.
     */
    C create();
}