/*
 * Copyright (C) 2020 Guus Lieben
 *
 * This framework is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.selene.di.inject.modules;

import com.google.inject.AbstractModule;

import org.dockbox.selene.di.InjectConfiguration;
import org.dockbox.selene.di.annotations.BindingMeta;
import org.dockbox.selene.di.inject.Binder;
import org.dockbox.selene.di.inject.Injector;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

public class InjectConfigurationModule extends AbstractModule implements Binder {

    private final InjectConfiguration configuration;
    private final Injector injector;

    public InjectConfigurationModule(InjectConfiguration configuration, Injector injector) {
        this.configuration = configuration;
        this.injector = injector;
    }

    @Override
    protected void configure() {
        this.configuration.setBinder(this);
        this.configuration.collect();
    }

    @Override
    public <C, T extends C, A extends Annotation> void provide(Class<C> contract, Supplier<? extends T> supplier) {
        this.bind(contract).toProvider(supplier::get);
    }

    @Override
    public <C, T extends C, A extends Annotation> void provide(Class<C> contract, Supplier<? extends T> supplier, BindingMeta meta) {
        this.bind(contract).annotatedWith(meta).toProvider(supplier::get);
    }

    @Override
    public <C, T extends C> void bind(Class<C> contract, Class<? extends T> implementation) {
        this.bind(contract).to(implementation);
    }

    @Override
    public <C, T extends C> void bind(Class<C> contract, Class<? extends T> implementation, BindingMeta meta) {
        this.bind(contract).annotatedWith(meta).to(implementation);
    }

    @Override
    public <C, T extends C> void bind(Class<C> contract, T instance) {
        this.bind(contract).toInstance(instance);
    }

    @Override
    public <C, T extends C> void bind(Class<C> contract, T instance, BindingMeta meta) {
        this.bind(contract).annotatedWith(meta).toInstance(instance);
    }

    @Override
    public <C, T extends C> void wire(Class<C> contract, Class<? extends T> implementation) {
        this.injector.wire(contract, implementation);
    }
}
