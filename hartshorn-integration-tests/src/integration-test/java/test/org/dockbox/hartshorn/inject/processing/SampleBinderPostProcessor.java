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

import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.inject.binding.HierarchicalBinder;
import org.dockbox.hartshorn.inject.processing.HierarchicalBinderPostProcessor;
import org.dockbox.hartshorn.inject.scope.Scope;

public class SampleBinderPostProcessor implements HierarchicalBinderPostProcessor {

    public static final String HELLO_WORLD = "Hello, World!";

    @Override
    public void process(InjectionCapableApplication application, Scope scope, HierarchicalBinder binder) {
        binder.bind(String.class).singleton(HELLO_WORLD);
    }

    @Override
    public int priority() {
        return 0;
    }
}
