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

package org.dockbox.hartshorn.inject.populate;

import org.dockbox.hartshorn.context.DefaultContext;
import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.util.introspect.view.TypeView;

/**
 * A context that is used during the population of a component. This includes the instance that is being populated, the
 * type of the instance, the application context and the original instance if the instance was proxied.
 *
 * @param <T> the type of the component
 *
 * @since 0.6.0
 *
 * @author Guus Lieben
 */
public final class PopulateComponentContext<T> extends DefaultContext {

    private final T instance;
    private final T originalInstance;

    private final TypeView<T> type;
    private final InjectionCapableApplication application;

    public PopulateComponentContext(T instance, T originalInstance, TypeView<T> type, InjectionCapableApplication application) {
        this.instance = instance;
        this.originalInstance = originalInstance;
        this.type = type;
        this.application = application;
    }

    /**
     * Returns the instance that is being populated. This instance is not necessarily the same as the original instance
     * if the instance was proxied.
     *
     * @return the instance that is being populated
     */
    public T instance() {
        return this.instance;
    }

    /**
     * Returns the original instance if the instance was proxied. If the instance was not proxied, this method returns
     * the same instance as {@link #instance()}.
     *
     * @return the original instance if the instance was proxied, the same instance as {@link #instance()} otherwise
     */
    public T originalInstance() {
        return this.originalInstance;
    }

    /**
     * Returns the type of the component that is being populated.
     *
     * @return the type of the component that is being populated
     */
    public TypeView<T> type() {
        return this.type;
    }

    public InjectionCapableApplication application() {
        return this.application;
    }
}
