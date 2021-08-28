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

package org.dockbox.hartshorn.sponge.util;

import org.dockbox.hartshorn.api.CheckedSupplier;
import org.dockbox.hartshorn.api.domain.Exceptional;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.DefaultedRegistryValue;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryType;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

public class SpongeUtil {

    public static <T> Exceptional<T> await(final CompletableFuture<T> future) {
        return Exceptional.of(future::join);
    }

    public static <T> Exceptional<T> awaitSafe(final Future<Optional<T>> future) {
        try {
            return Exceptional.of(future.get());
        }
        catch (final ExecutionException | InterruptedException e) {
            return Exceptional.of(e);
        }
    }

    public static <T> T spongeReference(final RegistryType<T> registry, final String name) {
        return RegistryKey.of(registry, ResourceKey.sponge(name))
                .asDefaultedReference(Sponge::game)
                .get();
    }

    public static <T> Exceptional<T> fromNamespacedRegistry(final RegistryType<T> value, final String name) {
        if (name.indexOf(':') < 0) return Exceptional.empty();
        final String[] spaced = name.split(":", 2);
        return fromRegistry(value, ResourceKey.of(spaced[0], spaced[1]));
    }

    public static <T> Exceptional<T> fromRegistry(final RegistryType<T> value, final ResourceKey key) {
        final Exceptional<Registry<T>> registry = Exceptional.of(Sponge.game().findRegistry(value));
        return registry.map(r -> {
            final Optional<RegistryEntry<T>> entry = r.findEntry(key);
            return entry.map(RegistryEntry::value).orElse(null);
        });
    }

    public static <T> Exceptional<T> fromMCRegistry(final RegistryType<T> value, final String name) {
        return fromRegistry(value, ResourceKey.minecraft(name));
    }

    public static <T> Exceptional<T> fromSpongeRegistry(final RegistryType<T> value, final String name) {
        return fromRegistry(value, ResourceKey.sponge(name));
    }

    public static <T extends DefaultedRegistryValue> Exceptional<ResourceKey> location(final Exceptional<T> value, final DefaultedRegistryType<T> type) {
        return value.map(v -> v.asDefaultedReference(type).location());
    }

    public static <T extends DefaultedRegistryValue> DefaultedRegistryReference<T> key(final RegistryType<T> type, final String key) {
        return RegistryKey.of(type, ResourceKey.sponge(key)).asDefaultedReference(Sponge::game);
    }

    public static <T, C> T get(
            final Exceptional<? extends ValueContainer> container,
            final Key<? extends Value<C>> key,
            final Function<C, T> mapper,
            final CheckedSupplier<T> defaultValue) {
        return container
                .map(c -> c.get(key).orElse(null))
                .map(mapper::apply)
                .orElse(defaultValue).get();
    }

}
