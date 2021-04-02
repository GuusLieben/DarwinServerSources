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

package org.dockbox.selene.api.objects.keys;

import org.dockbox.selene.api.module.ModuleContainer;
import org.dockbox.selene.api.server.Selene;
import org.dockbox.selene.api.util.Reflect;
import org.jetbrains.annotations.NonNls;

public class TypedPersistentDataKey<T> implements PersistentDataKey<T> {

    @NonNls
    private final String name;
    @NonNls
    private final String id;
    private final ModuleContainer module;
    private final Class<T> type;

    public TypedPersistentDataKey(String name, String id, ModuleContainer module, Class<T> type) {
        this.name = name;
        this.id = id;
        this.module = module;
        this.type = type;
    }

    @Override
    public Class<T> getDataType() {
        return this.type;
    }

    @Override
    public String getRegisteringModuleId() {
        return this.module.id();
    }

    @Override
    public String getDataKeyId() {
        return this.id;
    }

    @Override
    public String getDataKeyName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        // Does not include the responsible module, as comparisons may be made against the StoredPersistentKey type
        // which uses the IntegratedModule.
        int result = this.id.hashCode();
        result = 31 * result + this.type.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypedPersistentDataKey)) return false;

        TypedPersistentDataKey<?> that = (TypedPersistentDataKey<?>) o;

        if (!this.id.equals(that.id)) return false;
        if (!this.module.equals(that.module) && !this.module.equals(Reflect.getModule(Selene.class))) return false;
        return this.type.equals(that.type);
    }
}
