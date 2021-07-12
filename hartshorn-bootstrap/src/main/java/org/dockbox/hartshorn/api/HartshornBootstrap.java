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

package org.dockbox.hartshorn.api;

import com.google.common.collect.Multimap;

import org.dockbox.hartshorn.api.annotations.UseBootstrap;
import org.dockbox.hartshorn.api.config.GlobalConfig;
import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.exceptions.Except;
import org.dockbox.hartshorn.di.InjectConfiguration;
import org.dockbox.hartshorn.di.InjectableBootstrap;
import org.dockbox.hartshorn.di.Modifier;
import org.dockbox.hartshorn.di.annotations.inject.InjectPhase;
import org.dockbox.hartshorn.di.annotations.inject.Required;
import org.dockbox.hartshorn.di.services.ComponentContainer;
import org.dockbox.hartshorn.util.HartshornUtils;
import org.dockbox.hartshorn.util.Reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import lombok.Getter;

/**
 * The global bootstrapping component which instantiates all configured services and provides access
 * to server information.
 */
public abstract class HartshornBootstrap extends InjectableBootstrap {

    @Getter
    private String version;
    private final Set<Method> postBootstrapActivations = HartshornUtils.emptyConcurrentSet();

    @Override
    public void create(String prefix, Class<?> activationSource, List<Annotation> activators, Multimap<InjectPhase, InjectConfiguration> configs, Modifier... modifiers) {
        activators.add(new UseBootstrap() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return UseBootstrap.class;
            }
        });
        super.create(prefix, activationSource, activators, configs, modifiers);

        final GlobalConfig globalConfig = this.context().get(GlobalConfig.class);
        Except.useStackTraces(globalConfig.stacktraces());
        Except.with(globalConfig.level());
        this.version = globalConfig.version();
    }

    public static boolean constructed() {
        return instance() != null;
    }

    public static HartshornBootstrap instance() {
        return (HartshornBootstrap) InjectableBootstrap.instance();
    }

    /**
     * Initiates a {@link Hartshorn} instance. Collecting integrated services and registering them to the
     * appropriate instances where required.
     */
    @Override
    public void init() {
        Hartshorn.log().info("Initialising Hartshorn " + this.context());
        for (Method postBootstrapActivation : this.postBootstrapActivations) {
            this.context().invoke(postBootstrapActivation);
        }
        // Ensure all services requiring a platform implementation have one present
        Reflect.types(Required.class).forEach(type -> {
            if (Reflect.children(type).isEmpty()) {
                this.handleMissingBinding(type);
            }
        });

    }

    protected void handleMissingBinding(Class<?> type) {
        throw new IllegalStateException("No implementation exists for [" + type.getCanonicalName() + "], this will cause functionality to misbehave or not function!");
    }

    void addPostBootstrapActivation(Method method, Class<?> type) {
        final Exceptional<ComponentContainer> container = Hartshorn.context().locator().container(type);

        if (container.present()) {
            final ComponentContainer componentContainer = container.get();
            final List<Class<? extends Annotation>> activators = componentContainer.activators();

            if (componentContainer.hasActivator() && activators.stream().allMatch(this.context()::hasActivator))
                this.postBootstrapActivations.add(method);
        }
    }

}
