/*
 * Copyright 2019-2023 the original author or authors.
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

package org.dockbox.hartshorn.component.processing.proxy;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.ComponentContainer;
import org.dockbox.hartshorn.component.ComponentKey;
import org.dockbox.hartshorn.component.processing.ComponentProcessingContext;
import org.dockbox.hartshorn.component.processing.FunctionalComponentPostProcessor;
import org.dockbox.hartshorn.proxy.ProxyFactory;

public abstract class ProxyDelegationPostProcessor<P> extends FunctionalComponentPostProcessor {

    protected abstract Class<P> parentTarget();

    @Override
    public final <T> T process(final ApplicationContext context, @Nullable final T instance, final ComponentContainer container, final ComponentProcessingContext<T> processingContext) {
        if (!processingContext.type().isChildOf(this.parentTarget())) return instance;

        final ProxyFactory<P> factory = processingContext.get(ComponentKey.of(ProxyFactory.class));
        if (factory == null) return instance;

        final P concreteDelegator = this.concreteDelegator(context, factory, this.parentTarget());

        if (this.skipConcreteMethods()) {
            // Ensure we keep the original instance as delegate, to avoid losing context. This rule is defined by the finalizing process.
            factory.delegate((P) instance);
            factory.delegateAbstract(this.parentTarget(), concreteDelegator);
        }
        else {
            factory.delegate(this.parentTarget(), concreteDelegator);
        }

        return instance;
    }

    protected P concreteDelegator(final ApplicationContext context, final ProxyFactory<P> handler, final Class<? extends P> parent) {
        return context.get(this.parentTarget());
    }

    protected boolean skipConcreteMethods() {
        return false;
    }
}