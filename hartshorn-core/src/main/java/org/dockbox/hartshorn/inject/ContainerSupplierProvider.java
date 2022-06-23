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

package org.dockbox.hartshorn.inject;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.util.Result;

import java.util.function.Supplier;

/**
 * A {@link Supplier} that is able to provide instances using the given {@link Supplier}.
 * If the {@link Supplier} is unable to provide an instance, an empty {@link Result}
 * will be returned without throwing an exception.
 *
 * @param <C> The type to be provided.
 * @author Guus Lieben
 * @since 21.4
 * @see Provider
 * @see ContextDrivenProvider
 */
public class ContainerSupplierProvider<C> implements Provider<C> {

    private final Supplier<ObjectContainer<C>> supplier;

    public ContainerSupplierProvider(final Supplier<ObjectContainer<C>> supplier) {
        this.supplier = supplier;
    }

    public Supplier<ObjectContainer<C>> supplier() {
        return this.supplier;
    }

    @Override
    public Result<ObjectContainer<C>> provide(final ApplicationContext context) {
        return Result.of(this.supplier::get);
    }
}