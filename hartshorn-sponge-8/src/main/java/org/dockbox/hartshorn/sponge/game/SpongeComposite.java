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

package org.dockbox.hartshorn.sponge.game;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.i18n.entry.DefaultResources;
import org.dockbox.hartshorn.api.keys.PersistentDataHolder;
import org.dockbox.hartshorn.api.keys.PersistentDataKey;
import org.dockbox.hartshorn.api.keys.StoredPersistentKey;
import org.dockbox.hartshorn.api.keys.TransactionResult;
import org.dockbox.hartshorn.util.HartshornUtils;
import org.dockbox.hartshorn.util.Reflect;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.MapValue;
import org.spongepowered.api.data.value.Value.Immutable;

import java.util.Map;

public interface SpongeComposite extends PersistentDataHolder {

    public static final Key<MapValue<String, Object>> COMPOSITE = Key.builder()
            .key(ResourceKey.of("hartshorn", "body_rotations"))
            .mapElementType(String.class, Object.class)
            .build();

    @Override
    default <T> Exceptional<T> get(PersistentDataKey<T> dataKey) {
        final Map<String, Object> data = this.raw();

        final Object value = data.get(dataKey.getId());
        if (value != null && Reflect.assigns(dataKey.getType(), value.getClass())) {
            //noinspection unchecked
            return Exceptional.of(() -> (T) value);
        }
        return Exceptional.empty();
    }

    @Override
    default <T> TransactionResult set(PersistentDataKey<T> dataKey, T value) {
        return this.holder().map(composite -> {
            final Map<String, Object> data = this.raw();
            data.put(dataKey.getId(), value);

            final DataTransactionResult result = composite.offer(COMPOSITE, data);
            if (result.isSuccessful()) return TransactionResult.success();
            else return TransactionResult.fail(DefaultResources.instance().getBindingFailure());
        }).get(() -> TransactionResult.fail(DefaultResources.instance().getReferenceLost()));
    }

    @Override
    default <T> void remove(PersistentDataKey<T> dataKey) {
        this.holder().present(composite -> {
            final Map<String, Object> data = this.raw();
            data.remove(dataKey.getId());
            composite.offer(COMPOSITE, data);
        });
    }

    @Override
    default Map<PersistentDataKey<?>, Object> getPersistentData() {
        final Exceptional<? extends DataHolder> dataHolder = this.holder();
        if (dataHolder.absent()) return HartshornUtils.emptyMap();

        final Map<PersistentDataKey<?>, Object> data = HartshornUtils.emptyMap();
        final DataHolder holder = dataHolder.get();

        for (Immutable<?> value : holder.getValues()) {
            final StoredPersistentKey key = StoredPersistentKey.of(value.key().key().asString());
            final Object dataValue = value.get();
            data.put(key, dataValue);
        }
        return data;
    }

    private Map<String, Object> raw() {
        return this.holder().map(composite -> composite
                .get(COMPOSITE)
                .orElseGet(HartshornUtils::emptyMap)
        ).get();
    }

    private Exceptional<? extends DataHolder.Mutable> holder() {
        return this.getDataHolder().absent(() -> {
            Hartshorn.log().warn("Attempted to access data holder in " + this.getClass().getSimpleName() + " but none was present.");
        });
    }

    Exceptional<? extends DataHolder.Mutable> getDataHolder();
}
