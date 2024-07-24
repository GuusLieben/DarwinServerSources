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

package test.org.dockbox.hartshorn.inject.callbacks;

import org.dockbox.hartshorn.inject.annotations.Initialize;
import org.dockbox.hartshorn.inject.annotations.Component;

import org.dockbox.hartshorn.inject.annotations.Inject;

@Component
public class TypeWithPostConstructableInjectField {

    @Inject
    @Initialize(true)
    private SelfInitializationListener listener;

    public SelfInitializationListener postConstructableObject() {
        return this.listener;
    }
}