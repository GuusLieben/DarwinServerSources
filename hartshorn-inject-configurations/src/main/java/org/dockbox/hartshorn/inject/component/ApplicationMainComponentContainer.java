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

package org.dockbox.hartshorn.inject.component;

import org.dockbox.hartshorn.inject.provider.LifecycleType;
import org.dockbox.hartshorn.util.introspect.view.TypeView;

/**
 * A specialized {@link ComponentContainer} for the main class of an application. There should only be one of these
 * per application.
 *
 * @param <T> The type of the main class
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class ApplicationMainComponentContainer<T> implements ComponentContainer<T> {

    private final TypeView<T> component;

    public ApplicationMainComponentContainer(TypeView<T> component) {
        this.component = component;
    }

    @Override
    public String id() {
        return ComponentDescriber.id(this.component);
    }

    @Override
    public String name() {
        return ComponentDescriber.name(this.component);
    }

    @Override
    public TypeView<T> type() {
        return this.component;
    }

    @Override
    public LifecycleType lifecycle() {
        return LifecycleType.SINGLETON;
    }

    @Override
    public boolean lazy() {
        return false;
    }

    @Override
    public boolean permitsProxying() {
        return false;
    }

    @Override
    public boolean permitsProcessing() {
        return false;
    }
}
