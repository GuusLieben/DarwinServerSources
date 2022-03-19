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

package org.dockbox.hartshorn.core.boot;

import org.dockbox.hartshorn.application.HartshornApplication;
import org.dockbox.hartshorn.core.boot.activators.AbstractActivator;
import org.dockbox.hartshorn.core.boot.activators.InterfaceActivator;
import org.dockbox.hartshorn.core.boot.activators.NonDecoratedActivator;
import org.dockbox.hartshorn.core.boot.activators.ValidActivator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HartshornApplicationTests {

    @Test
    void testCreationFailsWithAbsentDecorator() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HartshornApplication.create(NonDecoratedActivator.class, new String[0]));
    }

    @Test
    void testCreationFailsWithAbstractActivator() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HartshornApplication.create(AbstractActivator.class, new String[0]));
    }

    @Test
    void testCreationFailsWithInterfaceActivator() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HartshornApplication.create(InterfaceActivator.class, new String[0]));
    }

    @Test
    void testCreationSucceedsWithValidActivator() {
        Assertions.assertDoesNotThrow(() -> HartshornApplication.create(ValidActivator.class, new String[0]));
    }
}
