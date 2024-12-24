/*
 * Copyright 2019-2024 the original author or authors.
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

package org.dockbox.hartshorn.inject.scope;

import org.dockbox.hartshorn.reporting.DiagnosticsPropertyCollector;
import org.dockbox.hartshorn.util.TypeUtils;
import org.dockbox.hartshorn.util.introspect.ParameterizableType;

import java.util.Objects;

/**
 * Specialized {@link ScopeKey} for {@link ScopeAdapter} instances. This tracks both the adaptee type
 * and the scope adapter type.
 *
 * @see ScopeAdapter
 * @see ScopeKey
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public class ScopeAdapterKey implements ScopeKey {

    private final ParameterizableType adapterType;
    private final ParameterizableType adapteeType;

    protected ScopeAdapterKey(ParameterizableType adapterType) {
        if (!ScopeAdapter.class.isAssignableFrom(adapterType.type())) {
            throw new IllegalArgumentException("The given type is not a ScopeAdapter");
        }
        if (adapterType.parameters().isEmpty()) {
            throw new IllegalArgumentException("The given type is not a parameterized ScopeAdapter");
        }
        if (adapterType.parameters().size() > 1) {
            throw new IllegalArgumentException("The given type is a ScopeAdapter with too many parameters");
        }
        this.adapterType = adapterType;
        this.adapteeType = adapterType.parameters().getFirst();
    }

    /**
     * Creates a new key for the given adapter. The adaptee type is derived from the adapter.
     *
     * @param adapter the adapter to create a key for
     * @param <T> the adaptee type
     *
     * @return the key
     */
    public static <T> ScopeAdapterKey of(ScopeAdapter<T> adapter) {
        ParameterizableType adapteeType = adapter.adapteeType();
        ParameterizableType adapterType = ParameterizableType.builder(ScopeAdapter.class).parameters(adapteeType).build();
        return new ScopeAdapterKey(TypeUtils.unchecked(adapterType, ParameterizableType.class));
    }

    /**
     * Creates a new key for the given adaptee type.
     *
     * @param type the adaptee type
     * @return the key
     */
    public static ScopeAdapterKey of(ParameterizableType type) {
        return new ScopeAdapterKey(type);
    }

    /**
     * Returns the type of the adaptee. This contains the full type information, including any type
     * parameters. This is not the type of the scope itself, but of the instance that is wrapped by
     * the adapter.
     *
     * @return the type of the adaptee
     */
    public ParameterizableType adapteeType() {
        return this.adapteeType;
    }

    @Override
    public String name() {
        return this.adapterType.toString();
    }

    @Override
    public ParameterizableType scopeType() {
        return this.adapterType;
    }

    @Override
    public boolean equals(Object object) {
        if(this == object) {
            return true;
        }
        if(!(object instanceof ScopeAdapterKey that)) {
            return false;
        }
        // Note that adapterType already contains adaptee type, so no need to check that separately
        return Objects.equals(this.adapterType, that.adapterType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.adapterType);
    }

    @Override
    public void report(DiagnosticsPropertyCollector collector) {
        collector.property("adapterType").writeDelegate(this.adapterType);
        collector.property("adapteeType").writeDelegate(this.adapteeType);
    }
}
