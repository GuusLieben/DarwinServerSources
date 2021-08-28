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

package org.dockbox.hartshorn.di;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.di.binding.BindingHierarchy;
import org.dockbox.hartshorn.di.binding.ContextDrivenProvider;
import org.dockbox.hartshorn.di.binding.Provider;
import org.dockbox.hartshorn.di.context.ApplicationContext;
import org.dockbox.hartshorn.util.HartshornUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.inject.Named;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NativeBindingHierarchy<C> implements BindingHierarchy<C> {

    @Getter private final Key<C> key;
    @Getter private final ApplicationContext applicationContext;
    private final Map<Integer, Provider<C>> bindings = new TreeMap<>();

    @Override
    public Set<Provider<C>> providers() {
        return HartshornUtils.asUnmodifiableSet(this.bindings.values());
    }

    @Override
    public BindingHierarchy<C> add(final Provider<C> provider) {
        return this.add(-1, provider);
    }

    @Override
    public BindingHierarchy<C> add(final int priority, final Provider<C> provider) {
        if (this.bindings.containsKey(priority)) {
            ApplicationContextAware.instance().log().warn(("There is already a provider for %s with priority %d. It will be overwritten! " +
                    "To avoid unexpected behavior, ensure the priority is not already present. Current hierarchy: %s").formatted(this.key()
                    .contract().getSimpleName(), priority, this));
        }
        this.bindings.put(priority, provider);
        return this;
    }

    @Override
    public BindingHierarchy<C> merge(final BindingHierarchy<C> hierarchy) {
        final BindingHierarchy<C> merged = new NativeBindingHierarchy<>(this.key(), this.applicationContext);
        // Low priority, other
        for (final Entry<Integer, Provider<C>> entry : hierarchy) {
            merged.add(entry.getKey(), entry.getValue());
        }
        // High priority, self
        for (final Entry<Integer, Provider<C>> entry : this) {
            merged.add(entry.getKey(), entry.getValue());
        }
        return merged;
    }

    @Override
    public int size() {
        return this.bindings.size();
    }

    @Override
    public Exceptional<Provider<C>> get(final int priority) {
        return Exceptional.of(this.bindings.getOrDefault(priority, null));
    }

    @Override
    public String toString() {
        final String contract = this.key().contract().getSimpleName();
        final Named named = this.key().named();
        String name = "";
        if (named != null) {
            name = "::" + named.value();
        }
        final String hierarchy = this.bindings.entrySet().stream()
                .map(entry -> {
                    final Provider<C> value = entry.getValue();
                    String target = value.toString();
                    if (value instanceof ContextDrivenProvider contextDrivenProvider) {
                        target = contextDrivenProvider.context().name();
                    }
                    return "%s: %s".formatted(String.valueOf(entry.getKey()), target);
                })
                .collect(Collectors.joining(" -> "));

        return "Hierarchy[%s%s]: %s".formatted(contract, name, hierarchy);
    }

    @NotNull
    @Override
    public Iterator<Entry<Integer, Provider<C>>> iterator() {
        return this.bindings.entrySet().iterator();
    }
}
