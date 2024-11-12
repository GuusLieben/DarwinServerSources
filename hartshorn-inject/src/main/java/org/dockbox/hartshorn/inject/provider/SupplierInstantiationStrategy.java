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

package org.dockbox.hartshorn.inject.provider;

import java.util.function.Supplier;

import org.dockbox.hartshorn.inject.ComponentRequestContext;
import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.util.ApplicationException;
import org.dockbox.hartshorn.util.Tristate;
import org.dockbox.hartshorn.util.function.CheckedSupplier;
import org.dockbox.hartshorn.util.option.Option;

/**
 * A {@link Supplier} that is able to provide instances using the given {@link Supplier}. If the
 * {@link Supplier} is unable to provide an instance, an empty {@link Option} will be returned
 * without throwing an exception.
 *
 * @param supplier The {@link Supplier} that is able to provide instances.
 * @param <C> The type to be provided.
 *
 * @see InstantiationStrategy
 *
 * @since 0.4.3
 *
 * @author Guus Lieben
 */
public record SupplierInstantiationStrategy<C>(CheckedSupplier<C> supplier) implements NonTypeAwareInstantiationStrategy<C> {

    @Override
    public Option<ObjectContainer<C>> provide(InjectionCapableApplication application, ComponentRequestContext requestContext) throws ApplicationException {
        C instance = this.supplier.get();
        return Option.of(instance).map(ComponentObjectContainer::ofPrototype);
    }

    @Override
    public LifecycleType defaultLifecycle() {
        return LifecycleType.PROTOTYPE;
    }

    @Override
    public Tristate defaultLazy() {
        return Tristate.TRUE;
    }
}
