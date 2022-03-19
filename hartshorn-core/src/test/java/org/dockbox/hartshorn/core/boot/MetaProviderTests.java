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

import org.dockbox.hartshorn.application.Hartshorn;
import org.dockbox.hartshorn.application.MetaProviderImpl;
import org.dockbox.hartshorn.inject.MetaProvider;
import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;
import org.dockbox.hartshorn.inject.TypedOwner;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@HartshornTest
public class MetaProviderTests {

    @Inject
    private ApplicationContext applicationContext;

    public ApplicationContext applicationContext() {
        return this.applicationContext;
    }

    @Test
    void testComponentTypeUsesComponentAlias() {
        final MetaProvider lookup = new MetaProviderImpl(this.applicationContext());
        final TypedOwner owner = lookup.lookup(TypeContext.of(EmptyComponent.class));
        Assertions.assertNotNull(owner);
        Assertions.assertEquals("component", owner.id());
    }

    @Test
    void testUsesProjectId() {
        final MetaProvider lookup = new MetaProviderImpl(this.applicationContext());
        final TypedOwner owner = lookup.lookup(TypeContext.of(Hartshorn.class));
        Assertions.assertNotNull(owner);
        Assertions.assertEquals(Hartshorn.PROJECT_ID, owner.id());

    }

    @Test
    void testServiceUsesServiceId() {
        final MetaProvider lookup = new MetaProviderImpl(this.applicationContext());
        final TypedOwner owner = lookup.lookup(TypeContext.of(EmptyService.class));
        Assertions.assertNotNull(owner);
        Assertions.assertEquals("empty", owner.id());
    }

    @Test
    void testUnknownIsSelf() {
        final MetaProvider lookup = new MetaProviderImpl(this.applicationContext());
        final TypedOwner owner = lookup.lookup(TypeContext.VOID);
        Assertions.assertNotNull(owner);
        Assertions.assertEquals("void", owner.id());
    }
}
