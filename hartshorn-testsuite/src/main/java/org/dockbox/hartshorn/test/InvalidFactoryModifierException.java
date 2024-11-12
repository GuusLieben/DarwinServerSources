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

package org.dockbox.hartshorn.test;

import org.dockbox.hartshorn.launchpad.launch.ApplicationBuilder;
import org.dockbox.hartshorn.test.annotations.CustomizeTests;
import org.dockbox.hartshorn.util.ApplicationRuntimeException;

/**
 * Thrown when a method annotated with {@link CustomizeTests} is not a valid factory customizer. A valid factory customizer
 * is expected to be a <i>static</i> method that <i>accepts no arguments</i> and <i>returns void</i>.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class InvalidFactoryModifierException extends ApplicationRuntimeException {
    public InvalidFactoryModifierException(String what, Class<?> actual) {
        super("Invalid " + what + " for @HartshornFactory modifier, expected " + ApplicationBuilder.class.getSimpleName() + " but got " + actual.getSimpleName());
    }

    public InvalidFactoryModifierException(String message) {
        super(message);
    }
}
