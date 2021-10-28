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

package org.dockbox.hartshorn.core.services;

import org.dockbox.hartshorn.core.annotations.proxy.UseProxying;
import org.dockbox.hartshorn.core.binding.Bindings;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.inject.InjectionModifier;
import org.dockbox.hartshorn.core.properties.Attribute;
import org.dockbox.hartshorn.core.proxy.ProxyAttribute;
import org.dockbox.hartshorn.core.proxy.ProxyHandler;
import org.dockbox.hartshorn.core.proxy.ProxyUtil;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

public class ProxyInjectionModifier implements InjectionModifier<UseProxying> {

    @Override
    public <T> boolean preconditions(final ApplicationContext context, final TypeContext<T> type, @Nullable final T instance, final Attribute<?>... properties) {
        // Unchecked as ProxyProperty has generic type parameters
        //noinspection unchecked
        return Bindings.has(ProxyAttribute.class, properties);
    }

    @Override
    public <T> T process(final ApplicationContext context, final TypeContext<T> type, @Nullable final T instance, final Attribute<?>... properties) {
        try {
            final ProxyHandler<T> handler = ProxyUtil.handler(type, instance);

            for (final Attribute<?> property : properties) {
                if (property instanceof ProxyAttribute) {
                    //noinspection unchecked
                    final ProxyAttribute<T, ?> proxyAttribute = (ProxyAttribute<T, ?>) property;
                    handler.delegate(proxyAttribute);
                }
            }
            return handler.proxy();
        }
        catch (final InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException | ClassCastException e) {
            return instance;
        }
    }

    @Override
    public Class<UseProxying> activator() {
        return UseProxying.class;
    }
}