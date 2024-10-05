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

package test.org.dockbox.hartshorn.inject.processing;

import org.dockbox.hartshorn.inject.ComponentKey;
import org.dockbox.hartshorn.inject.annotations.Inject;
import org.dockbox.hartshorn.inject.binding.BindingHierarchy;
import org.dockbox.hartshorn.inject.binding.HierarchicalBinder;
import org.dockbox.hartshorn.inject.provider.ComponentProvider;
import org.dockbox.hartshorn.test.junit.HartshornIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@HartshornIntegrationTest(includeBasePackages = false, binderPostProcessors = SampleBinderPostProcessor.class)
public class BinderProcessorTests {

    @Test
    void testBinderPostProcessorIsCalled(@Inject HierarchicalBinder binder, @Inject ComponentProvider provider) {
        BindingHierarchy<String> hierarchy = binder.hierarchy(ComponentKey.of(String.class));
        Assertions.assertTrue(hierarchy.size() > 0);

        String message = provider.get(String.class);
        Assertions.assertEquals(SampleBinderPostProcessor.HELLO_WORLD, message);
    }
}
