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

package org.dockbox.hartshorn.inject;

/**
 * A 'dumb' object factory, which is typically not aware of any IoC container or other context. It is used to create
 * instances of objects without any additional context. This is typically used in early bootstrapping of the application,
 * before the IoC container is fully initialized.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
@FunctionalInterface
public interface ObjectFactory {

    /**
     * Creates an instance of the given type. The type is expected to have a no-argument constructor.
     *
     * @param type the type to create an instance of
     * @return the created instance
     * @param <T> the type of the instance
     */
    <T> T create(Class<T> type);
}
