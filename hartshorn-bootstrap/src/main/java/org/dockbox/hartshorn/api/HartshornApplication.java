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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.exceptions.Except;
import org.dockbox.hartshorn.di.InjectConfiguration;
import org.dockbox.hartshorn.di.InjectableBootstrap;
import org.dockbox.hartshorn.di.Modifier;
import org.dockbox.hartshorn.di.annotations.activate.Activator;
import org.dockbox.hartshorn.di.annotations.inject.InjectConfig;
import org.dockbox.hartshorn.di.annotations.inject.InjectPhase;
import org.dockbox.hartshorn.util.HartshornUtils;
import org.dockbox.hartshorn.util.Reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class HartshornApplication {

    public static Runnable create(Class<?> activator, Modifier... modifiers) {
        try {
            final long start = System.currentTimeMillis();
            final Activator annotation = verifyActivator(activator);
            final Class<? extends InjectableBootstrap> bootstrap = annotation.value();
            final InjectableBootstrap injectableBootstrap = instance(bootstrap);

            String prefix = "".equals(annotation.prefix()) ? activator.getPackage().getName() : annotation.prefix();

            Multimap<InjectPhase, InjectConfiguration> configurations = ArrayListMultimap.create();
            for (InjectConfig config : annotation.configs()) {
                configurations.put(config.phase(), instance(config.value()));
            }

            final List<String> prefixes = HartshornUtils.asList(Hartshorn.PACKAGE_PREFIX);
            if (!prefix.startsWith(Hartshorn.PACKAGE_PREFIX)) prefixes.add(prefix);

            injectableBootstrap.create(
                    prefixes,
                    activator,
                    HartshornUtils.emptyList(),
                    configurations,
                    modifiers);
            final long creationTime = System.currentTimeMillis() - start;

            return () -> {
                final long initStart = System.currentTimeMillis();
                injectableBootstrap.init();
                final long initTime = System.currentTimeMillis() - initStart;
                Hartshorn.log().info("Started " + Hartshorn.PROJECT_NAME + " in " + (creationTime + initTime) + "ms");
            };
        }
        catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            Except.handle("Could not bootstrap application " + activator.getSimpleName(), e);
            return () -> {
                throw new RuntimeException("Hartshorn could not be bootstrapped, see cause for details", e);
            };
        }
    }

    private static <T> T instance(Class<T> type) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        final Constructor<T> constructor = type.getConstructor();
        return constructor.newInstance();
    }

    private static Activator verifyActivator(Class<?> activator) {
        final Exceptional<Activator> annotation = Reflect.annotation(activator, Activator.class);
        if (annotation.absent())
            throw new IllegalArgumentException("Application type should be decorated with @Activator");

        if (Reflect.isAbstract(activator))
            throw new IllegalArgumentException("Bootstrap type cannot be abstract, got " + activator.getSimpleName());

        return annotation.get();
    }

}
