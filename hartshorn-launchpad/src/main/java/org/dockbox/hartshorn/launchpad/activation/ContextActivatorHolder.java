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

package org.dockbox.hartshorn.launchpad.activation;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Supplier;

import org.dockbox.hartshorn.util.option.Option;

/**
 * A lazy {@link ActivatorHolder} that provides activators from a {@link ServiceActivatorContext} when needed.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class ContextActivatorHolder implements ActivatorHolder {

    private final Supplier<Option<ServiceActivatorContext>> contextProvider;

    protected ContextActivatorHolder(Supplier<Option<ServiceActivatorContext>> contextProvider) {
        this.contextProvider = contextProvider;
    }

    public static ContextActivatorHolder of(Supplier<Option<ServiceActivatorContext>> contextProvider) {
        return new ContextActivatorHolder(contextProvider);
    }

    public static ContextActivatorHolder of(ServiceActivatorContext context) {
        return new ContextActivatorHolder(() -> Option.of(context));
    }

    @Override
    public Set<Annotation> activators() {
        return this.contextProvider.get()
                .map(ServiceActivatorContext::activators)
                .orElseGet(Set::of);
    }

    @Override
    public <A> Option<A> activator(Class<A> activator) {
        return this.contextProvider.get()
                .map(context -> context.activator(activator));
    }

    @Override
    public boolean hasActivator(Class<? extends Annotation> activator) {
        return this.contextProvider.get()
                .map(context -> context.hasActivator(activator))
                .orElseGet(() -> false);
    }
}
