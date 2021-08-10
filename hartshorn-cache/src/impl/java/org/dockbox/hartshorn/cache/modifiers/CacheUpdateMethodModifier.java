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

package org.dockbox.hartshorn.cache.modifiers;

import org.dockbox.hartshorn.cache.annotations.UpdateCache;
import org.dockbox.hartshorn.cache.context.CacheContext;
import org.dockbox.hartshorn.cache.context.CacheMethodContext;
import org.dockbox.hartshorn.cache.context.CacheMethodContextImpl;
import org.dockbox.hartshorn.di.context.ApplicationContext;
import org.dockbox.hartshorn.proxy.handle.ProxyFunction;
import org.dockbox.hartshorn.proxy.service.MethodProxyContext;

import java.lang.reflect.Parameter;

/**
 * The {@link org.dockbox.hartshorn.proxy.service.ServiceAnnotatedMethodModifier} responsible for {@link UpdateCache}
 * decorated methods. This delegates functionality to the underlying {@link org.dockbox.hartshorn.cache.CacheManager}
 * to update specific {@link org.dockbox.hartshorn.cache.Cache caches}.
 */
public class CacheUpdateMethodModifier extends CacheServiceModifier<UpdateCache> {

    @Override
    protected CacheMethodContext context(MethodProxyContext<?> context) {
        final UpdateCache update = context.annotation(UpdateCache.class);
        return new CacheMethodContextImpl(update.manager(), update.value(), null);
    }

    @Override
    protected <T, R> ProxyFunction<T, R> process(ApplicationContext context, MethodProxyContext<T> methodContext, CacheContext cacheContext) {
        return (instance, args, proxyContext) -> {
            final Object o = args[0];
            cacheContext.manager().update(cacheContext.name(), o);
            return null; // Should be void anyway
        };
    }

    @Override
    public <T> boolean preconditions(ApplicationContext context, MethodProxyContext<T> methodContext) {
        final Parameter[] parameters = methodContext.method().getParameters();
        return parameters.length == 1;
    }

    @Override
    public Class<UpdateCache> annotation() {
        return UpdateCache.class;
    }
}
