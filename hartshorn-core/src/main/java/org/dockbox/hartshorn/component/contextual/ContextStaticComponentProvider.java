/*
 * Copyright 2019-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dockbox.hartshorn.component.contextual;

import org.dockbox.hartshorn.component.ComponentKey;
import org.dockbox.hartshorn.inject.Context;
import org.dockbox.hartshorn.util.StringUtilities;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import jakarta.inject.Inject;

public class ContextStaticComponentProvider implements StaticComponentProvider {

    private final StaticComponentContext staticComponentContext;

    @Inject
    public ContextStaticComponentProvider(@Context final StaticComponentContext staticComponentContext) {
        this.staticComponentContext = staticComponentContext;
    }

    private Predicate<StaticComponentContainer<?>> typeFilter(final Class<?> type) {
        return ref -> ref.type().isChildOf(type);
    }

    private Predicate<StaticComponentContainer<?>> idFilter(final String id) {
        if (StringUtilities.empty(id)) return ref -> true;
        return ref -> ref.id().equals(id);
    }

    private Predicate<StaticComponentContainer<?>> typeAndIdFilter(final Class<?> type, final String id) {
        return this.typeFilter(type).and(this.idFilter(id));
    }

    @Override
    public <T> T first(final Class<T> type) {
        return this.first(type, this.typeFilter(type));
    }

    @Override
    public <T> T first(final Class<T> type, final String id) {
        return this.first(type, this.typeAndIdFilter(type, id));
    }

    @Override
    public <T> T first(final ComponentKey<T> key) {
        if (key.name() != null) return this.first(key.type(), key.name());
        return this.first(key.type());
    }

    private <T> T first(final Class<T> type, final Predicate<StaticComponentContainer<?>> predicate) {
        return this.stream(type, predicate)
                .findFirst()
                .orElse(null);
    }

    @Override
    public <T> List<T> all(final Class<T> type) {
        return this.stream(type, this.typeFilter(type))
                .toList();
    }

    @Override
    public <T> List<T> all(final Class<T> type, final String id) {
        return this.stream(type, this.typeAndIdFilter(type, id))
                .toList();
    }

    @Override
    public <T> List<T> all(final ComponentKey<T> key) {
        if (key.name() != null) return this.all(key.type(), key.name());
        else return this.all(key.type());
    }

    private <T>Stream<T> stream(final Class<T> type, final Predicate<StaticComponentContainer<?>> predicate) {
        return this.staticComponentContext.containers().stream()
                .filter(predicate)
                .map(StaticComponentContainer::instance)
                .map(type::cast);
    }
}