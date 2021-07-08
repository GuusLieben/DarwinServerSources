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

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.domain.SimpleTypedOwner;
import org.dockbox.hartshorn.api.domain.TypedOwner;
import org.dockbox.hartshorn.api.entity.annotations.Entity;
import org.dockbox.hartshorn.di.InjectorMetaProvider;
import org.dockbox.hartshorn.di.services.ServiceContainer;
import org.dockbox.hartshorn.util.Reflect;

public class SimpleMetaProvider extends InjectorMetaProvider {

    @Override
    public TypedOwner lookup(Class<?> type) {
        if (type.isAnnotationPresent(Entity.class)) {
            return SimpleTypedOwner.of(type.getAnnotation(Entity.class).value());
        }
        else if (Hartshorn.class.equals(type)) {
            return SimpleTypedOwner.of(Hartshorn.PROJECT_ID);
        }
        else {
            final Exceptional<ServiceContainer> container = Hartshorn.context().locator().container(type);
            if (container.present()) {
                final ServiceContainer service = container.get();
                if (Reflect.notVoid(service.getOwner())) return this.lookup(service.getOwner());
            }
        }
        return super.lookup(type);
    }
}
