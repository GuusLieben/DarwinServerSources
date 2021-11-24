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

package org.dockbox.hartshorn.persistence.hibernate;

import org.dockbox.hartshorn.core.proxy.ProxyLookup;
import org.hibernate.proxy.HibernateProxy;

public class HibernateProxyLookup implements ProxyLookup {

    @Override
    public <T> Class<T> unproxy(final T instance) {
        if (instance instanceof HibernateProxy hibernateProxy) {
            return (Class<T>) hibernateProxy.getHibernateLazyInitializer().getPersistentClass();
        }
        return instance != null ? (Class<T>) instance.getClass() : null;
    }

    @Override
    public boolean isProxy(final Object instance) {
        return instance instanceof HibernateProxy;
    }

    @Override
    public boolean isProxy(final Class<?> candidate) {
        return HibernateProxy.class.isAssignableFrom(candidate);
    }
}
