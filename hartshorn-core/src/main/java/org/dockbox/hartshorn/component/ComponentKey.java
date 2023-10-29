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

package org.dockbox.hartshorn.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.dockbox.hartshorn.inject.Enable;
import org.dockbox.hartshorn.util.StringUtilities;
import org.dockbox.hartshorn.util.introspect.ElementAnnotationsIntrospector;
import org.dockbox.hartshorn.util.introspect.ParameterizableType;
import org.dockbox.hartshorn.util.introspect.view.ParameterView;
import org.dockbox.hartshorn.util.introspect.view.TypeView;

import jakarta.inject.Named;

/**
 * A key that can be used to identify a component. This contains required metadata to identify a component, such as
 * its type, name, scope and whether it should be enabled on provisioning.
 *
 * <p>Component keys contain a {@link ParameterizableType} that describes the type of the component. This type can
 * be parameterized. Therefore, key instances differentiate between e.g. {@code List<String>} and {@code List<Integer>}.
 *
 * <p>Keys are immutable, to build a new key based on an existing key, use {@link #mutable()}.
 *
 * @see ComponentProvider#get(ComponentKey)
 * @see ComponentKey#builder(Class)
 *
 * @param <T> the type of the component
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public final class ComponentKey<T> {

    private final ParameterizableType<T> type;
    private final String name;
    private final Scope scope;
    private final boolean enable;

    private ComponentKey(ParameterizableType<T> type, String name, Scope scope, boolean enable) {
        this.type = type;
        this.name = name;
        this.scope = scope;
        this.enable = enable;
    }

    /**
     * Creates a new builder for a component key of the given type. If the type is parameterized, the key will
     * be for the raw type.
     *
     * @param type the type of the component
     * @return a new builder
     * @param <T> the type of the component
     */
    public static <T> Builder<T> builder(Class<T> type) {
        return new Builder<>(new ParameterizableType<>(type));
    }

    /**
     * Creates a new builder for a component key of the given type. If the type is parameterized, the key will
     * retain its parameterization.
     *
     * @param type the type of the component
     * @return a new builder
     * @param <T> the type of the component
     */
    public static <T> Builder<T> builder(TypeView<T> type) {
        return new Builder<>(new ParameterizableType<>(type));
    }

    /**
     * Creates a new builder for a component key of the given type. If the type is parameterized, the key will
     * retain its parameterization.
     *
     * @param type the type of the component
     * @return a new builder
     * @param <T> the type of the component
     */
    public static <T> Builder<T> builder(ParameterizableType<T> type) {
        return new Builder<>(type);
    }

    /**
     * Creates a new builder for a component key of the given type. If the type is parameterized, the key will
     * retain its parameterization.
     *
     * @param parameter the parameter of the component
     * @return a new builder
     * @param <T> the type of the component
     */
    public static <T> Builder<T> builder(ParameterView<T> parameter) {
        Builder<T> builder = builder(parameter.genericType());
        ElementAnnotationsIntrospector annotations = parameter.annotations();
        annotations.get(Named.class).peek(builder::name);
        annotations.get(Enable.class).peek(enable -> builder.enable(enable.value()));
        return builder;
    }

    /**
     * Creates a new component key of the given type. If the type is parameterized, the key will be for the raw type.
     *
     * @param type the type of the component
     * @return a new component key
     * @param <T> the type of the component
     */
    public static <T> ComponentKey<T> of(Class<T> type) {
        return ComponentKey.builder(type).build();
    }

    /**
     * Creates a new component key of the given type. If the type is parameterized, the key will retain its
     * parameterization.
     *
     * @param type the type of the component
     * @return a new component key
     * @param <T> the type of the component
     */
    public static <T> ComponentKey<T> of(TypeView<T> type) {
        return ComponentKey.builder(type).build();
    }

    /**
     * Creates a new component key of the given type. If the type is parameterized, the key will retain its
     * parameterization.
     *
     * @param type the type of the component
     * @return a new component key
     * @param <T> the type of the component
     */
    public static <T> ComponentKey<T> of(ParameterizableType<T> type) {
        return ComponentKey.builder(type).build();
    }

    /**
     * Creates a new named component key of the given type. If the type is parameterized, the key will be for the raw type.
     *
     * @param key the type of the component
     * @param name the name of the component
     * @return a new component key
     * @param <T> the type of the component
     */
    public static <T> ComponentKey<T> of(Class<T> key, String name) {
        return ComponentKey.builder(key).name(name).build();
    }

    /**
     * Creates a new named component key of the given type. If the type is parameterized, the key will retain its
     * parameterization.
     *
     * @param type the type of the component
     * @param named the name of the component
     * @return a new component key
     * @param <T> the type of the component
     */
    public static <T> ComponentKey<T> of(TypeView<T> type, String named) {
        return ComponentKey.of(type.type(), named);
    }

    /**
     * Creates a new named component key of the given type. If the type is parameterized, the key will retain its
     * parameterization.
     *
     * @param parameter the parameter of the component
     * @return a new component key
     * @param <T> the type of the component
     */
    public static <T> ComponentKey<T> of(ParameterView<T> parameter) {
        return ComponentKey.builder(parameter).build();
    }

    /**
     * Creates a new key builder based on this key. The builder will have the same type, name, scope and enable
     * values as this key. The builder can be used to create a new key with different values.
     *
     * @return a new builder
     */
    public Builder<T> mutable() {
        return new Builder<>(this);
    }

    /**
     * Creates a new view of this key. The view will have the same type and name as this key. Views are not attached
     * to a scope, and do not indicate whether the component should be enabled. This method is useful for comparing
     * keys, or for use in maps.
     *
     * <p>Views always retain the parameterization of the key.
     *
     * @return a new view
     */
    public ComponentKeyView<T> view() {
        return new ComponentKeyView<>(this);
    }

    /**
     * Returns the qualified name of this key. The qualified name is the name of the type, followed by the name of
     * the component, followed by the name of the scope. If the component has no name, the name is omitted. If the
     * component has no explicit scope, the default scope is {@link Scope#DEFAULT_SCOPE}.
     *
     * @param qualifyType whether the type should be qualified with its package name
     * @return the qualified name
     */
    public String qualifiedName(boolean qualifyType) {
        String nameSuffix = StringUtilities.empty(this.name) ? "" : ":" + this.name;
        String scopeName = this.scope.installableScopeType().name();
        String typeName = qualifyType ? this.type.type().getCanonicalName() : this.type.type().getSimpleName();
        return typeName + nameSuffix + " @ " + scopeName;
    }

    @Override
    public String toString() {
        return "ComponentKey<" + this.qualifiedName(false) + ">";
    }

    @Override
    public boolean equals(Object other) {
        if(this == other) {
            return true;
        }
        if(other == null || this.getClass() != other.getClass()) {
            return false;
        }
        ComponentKey<?> otherComponentKey = (ComponentKey<?>) other;
        return this.enable == otherComponentKey.enable
                && this.type.equals(otherComponentKey.type)
                && Objects.equals(this.name, otherComponentKey.name)
                && Objects.equals(this.scope, otherComponentKey.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.name, this.scope, this.enable);
    }

    /**
     * Returns the raw type of the component, excluding any type parameters.
     *
     * @return the raw type of the component
     */
    public Class<T> type() {
        return this.type.type();
    }

    /**
     * Returns the parameterized type of the component, including any type parameters.
     *
     * @return the parameterized type of the component
     */
    public ParameterizableType<T> parameterizedType() {
        return this.type;
    }

    /**
     * Returns the name of the component. If the component has no name, {@code null} is returned.
     *
     * @return the name of the component, or {@code null} if the component has no name
     */
    public String name() {
        return this.name;
    }

    /**
     * Returns the scope of the component. If the component has no explicit scope, the default scope is
     * {@link Scope#DEFAULT_SCOPE}.
     *
     * @return the scope of the component
     */
    public Scope scope() {
        return this.scope;
    }

    /**
     * Returns whether the component should be enabled on provisioning. If the component has no explicit enable
     * value, {@code true} is returned.
     *
     * @return whether the component should be enabled on provisioning
     */
    public boolean enable() {
        return this.enable;
    }

    /**
     * A builder for {@link ComponentKey}s. The builder can be used to create a new key based on an existing key,
     * or to create a new key from scratch.
     *
     * @param <T> the type of the component
     *
     * @see ComponentKey
     * @see ComponentKey#builder(Class)
     *
     * @since 0.5.0
     *
     * @author Guus Lieben
     */
    public static final class Builder<T> {

        private final ParameterizableType<T> type;
        private String name;
        private Scope scope = Scope.DEFAULT_SCOPE;
        private boolean enable = true;

        private Builder(ComponentKey<T> key) {
            this.type = key.type;
            this.name = key.name;
            this.scope = key.scope;
            this.enable = key.enable;
        }

        private Builder(ParameterizableType<T> type) {
            this.type = type;
        }

        public <U> Builder<U> type(Class<U> type) {
            return this.type(new ParameterizableType<>(type));
        }

        public <U> Builder<U> type(TypeView<U> type) {
            return this.type(new ParameterizableType<>(type));
        }

        public <U> Builder<U> type(ParameterizableType<U> type) {
            return builder(type)
                    .name(this.name)
                    .scope(this.scope)
                    .enable(this.enable);
        }

        public Builder<T> parameterClasses(Class<?>... parameterTypes) {
            return this.parameterClasses(List.of(parameterTypes));
        }

        public Builder<T> parameterClasses(List<Class<?>> parameterTypes) {
            List<ParameterizableType<?>> types = new ArrayList<>();
            for (Class<?> parameterType : parameterTypes) {
                types.add(new ParameterizableType<>(parameterType));
            }
            return this.parameterTypes(types);
        }

        public Builder<T> parameterTypes(ParameterizableType<?>... parameterTypes) {
            return this.parameterTypes(List.of(parameterTypes));
        }

        public Builder<T> parameterTypes(List<ParameterizableType<?>> parameterTypes) {
            this.type.parameters(parameterTypes);
            return this;
        }

        public Builder<T> name(String name) {
            this.name = StringUtilities.nullIfEmpty(name);
            return this;
        }

        public Builder<T> name(Named named) {
            if(named != null) {
                return this.name(named.value());
            }
            return this;
        }

        public Builder<T> scope(Scope scope) {
            this.scope = scope;
            return this;
        }

        public Builder<T> enable(boolean enable) {
            this.enable = enable;
            return this;
        }

        public ComponentKey<T> build() {
            return new ComponentKey<>(this.type, this.name, this.scope, this.enable);
        }
    }

    public static final class ComponentKeyView<T> {

        private final ParameterizableType<T> type;
        private final String name;

        private ComponentKeyView(ComponentKey<T> key) {
            this.type = key.type;
            this.name = key.name;
        }

        @Override
        public boolean equals(Object other) {
            if(this == other) {
                return true;
            }
            if(other == null || this.getClass() != other.getClass()) {
                return false;
            }
            ComponentKeyView<?> otherKeyView = (ComponentKeyView<?>) other;
            return Objects.equals(this.type, otherKeyView.type) && Objects.equals(this.name, otherKeyView.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.type, this.name);
        }
    }

}
