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

package org.dockbox.selene.di.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.internal.LinkedBindingImpl;

import org.dockbox.selene.api.domain.Exceptional;
import org.dockbox.selene.api.domain.tuple.Tuple;
import org.dockbox.selene.di.binding.BindingData;
import org.dockbox.selene.di.binding.Bindings;
import org.dockbox.selene.di.InjectConfiguration;
import org.dockbox.selene.di.annotations.BindingMeta;
import org.dockbox.selene.di.annotations.Binds;
import org.dockbox.selene.di.annotations.MultiBinds;
import org.dockbox.selene.di.inject.modules.GuicePrefixScannerModule;
import org.dockbox.selene.di.inject.modules.InjectConfigurationModule;
import org.dockbox.selene.di.inject.modules.InstanceMetaModule;
import org.dockbox.selene.di.inject.modules.StaticMetaModule;
import org.dockbox.selene.di.inject.modules.ProvisionMetaModule;
import org.dockbox.selene.di.inject.modules.StaticModule;
import org.dockbox.selene.di.inject.modules.InstanceModule;
import org.dockbox.selene.di.inject.modules.ProvisionModule;
import org.dockbox.selene.di.properties.AnnotationProperty;
import org.dockbox.selene.di.properties.BindingMetaProperty;
import org.dockbox.selene.di.properties.InjectorProperty;
import org.dockbox.selene.util.Reflect;
import org.dockbox.selene.util.SeleneUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import javax.inject.Named;

public class GuiceInjector implements Injector {

    private final transient Set<AbstractModule> modules = SeleneUtils.emptyConcurrentSet();
    private com.google.inject.Injector internalInjector;

    @Override
    public void reset() {
        this.internalInjector = null;
    }

    @Override
    public <T> Exceptional<T> get(Class<T> type, InjectorProperty<?>... additionalProperties) {
        return Exceptional.of(() -> {
            @SuppressWarnings("rawtypes") @Nullable
            Exceptional<Class> annotation = Bindings.value(AnnotationProperty.KEY, Class.class, additionalProperties);
            if (annotation.present() && annotation.get().isAnnotation()) {
                //noinspection unchecked
                return (T) this.rebuild().getInstance(Key.get(type, annotation.get()));
            }
            else {
                @Nullable Exceptional<BindingMeta> meta = Bindings.value(BindingMetaProperty.KEY, BindingMeta.class, additionalProperties);
                if (meta.present()) {
                    return this.rebuild().getInstance(Key.get(type, meta.get()));
                }
                else {
                    return this.rebuild().getInstance(type);
                }
            }
        });
    }

    /**
     * Creates a custom binding for a given contract and implementation using a custom {@link
     * AbstractModule}. Requires the implementation to extend the contract type.
     *
     * <p>The binding is created by Guice, and can be annotated using Guice supported annotations
     * (e.g. {@link com.google.inject.Singleton})
     *
     * @param <T>
     *         The type parameter of the contract
     * @param contract
     *         The class type of the contract
     * @param implementation
     *         The class type of the implementation
     */
    @Override
    public <C, T extends C> void bind(Class<C> contract, Class<? extends T> implementation) {
        AbstractModule localModule = new StaticModule<>(contract, implementation);
        this.modules.add(localModule);
        this.reset();
    }

    private Map<Key<?>, Binding<?>> getAllBindings() {
        Map<Key<?>, Binding<?>> bindings = SeleneUtils.emptyConcurrentMap();
        for (Entry<Key<?>, Binding<?>> entry : this.rebuild().getAllBindings().entrySet()) {
            Key<?> key = entry.getKey();
            Binding<?> binding = entry.getValue();
            try {
                if (binding.getProvider() instanceof ProvisionModule
                        || binding.getProvider() instanceof ProvisionMetaModule
                        || null == binding.getProvider().get()
                ) continue;

                Class<?> keyType = binding.getKey().getTypeLiteral().getRawType();
                Class<?> providerType = binding.getProvider().get().getClass();

                if (!keyType.equals(providerType) && null != providerType)
                    bindings.put(key, binding);
            }
            catch (ProvisionException | AssertionError ignored) {
            }
        }
        return bindings;
    }

    @Override
    public List<BindingData> getBindingData() {
        List<BindingData> data = SeleneUtils.emptyList();
        for (Entry<Key<?>, Binding<?>> entry : this.getAllBindings().entrySet()) {
            Key<?> key = entry.getKey();
            Binding<?> binding = entry.getValue();
            Key<?> bindingKey = binding.getKey();
            if (binding instanceof LinkedBindingImpl) {
                bindingKey = ((LinkedBindingImpl<?>) binding).getLinkedKey();
            }

            Class<?> rawKey = key.getTypeLiteral().getRawType();
            Annotation annotation = key.getAnnotation();
            Class<?> rawBinding = bindingKey.getTypeLiteral().getRawType();

            if (annotation instanceof BindingMeta) {
                data.add(new BindingData(rawKey, rawBinding, (BindingMeta) annotation));
            }
            else if (annotation instanceof Named) {
                data.add(new BindingData(rawKey, rawBinding, Bindings.meta(((Named) annotation).value())));
            }
            else if (annotation instanceof com.google.inject.name.Named) {
                data.add(new BindingData(rawKey, rawBinding, Bindings.meta(((com.google.inject.name.Named) annotation).value())));
            }
            else {
                data.add(new BindingData(rawKey, rawBinding));
            }
        }
        data.sort(Comparator.comparing(d -> d.getSource().getSimpleName()));
        return data;
    }

    @Override
    public <T> T populate(T type) {
        if (null != type) this.rebuild().injectMembers(type);
        return type;
    }

    @Override
    public <C, T extends C> void bind(Class<C> contract, T instance) {
        AbstractModule localModule = new InstanceModule<>(contract, instance);
        this.modules.add(localModule);
        this.reset();
    }

    @Override
    public <C, T extends C> void bind(Class<C> contract, T instance, BindingMeta meta) {
        AbstractModule localModule = new InstanceMetaModule<>(contract, instance, meta);
        this.modules.add(localModule);
        this.reset();
    }

    @Override
    public <C, T extends C, A extends Annotation> void bind(Class<C> contract, Class<? extends T> implementation, BindingMeta meta) {
        AbstractModule localModule = new StaticMetaModule<>(contract, implementation, meta);
        this.modules.add(localModule);
        this.reset();
    }

    @Override
    public void bind(String prefix) {
        Map<Key<?>, Class<?>> scannedBinders = this.scan(prefix);
        this.modules.add(new GuicePrefixScannerModule(scannedBinders));
    }

    @Override
    public void bind(InjectConfiguration configuration) {
        this.modules.add(new InjectConfigurationModule(configuration));
    }

    private com.google.inject.Injector rebuild() {
        if (null == this.internalInjector) {
            Collection<AbstractModule> modules = new ArrayList<>(this.modules);
            modules.addAll(this.modules);
            this.internalInjector = Guice.createInjector(modules);
        }
        return this.internalInjector;
    }

    @Override
    public <C, T extends C, A extends Annotation> void provide(Class<C> contract, Supplier<? extends T> supplier, BindingMeta meta) {
        this.modules.add(new ProvisionMetaModule<>(contract, supplier, meta));
        this.reset();
    }

    @Override
    public <C, T extends C, A extends Annotation> void provide(Class<C> contract, Supplier<? extends T> supplier) {
        this.modules.add(new ProvisionModule<>(contract, supplier));
        this.reset();
    }

    private Map<Key<?>, Class<?>> scan(String prefix) {
        Map<Key<?>, Class<?>> bindings = SeleneUtils.emptyMap();

        Collection<Class<?>> binders = Reflect.annotatedTypes(prefix, Binds.class);
        for (Class<?> binder : binders) {
            Binds bindAnnotation = binder.getAnnotation(Binds.class);
            Class<?> binds = bindAnnotation.value();
            Entry<Key<?>, Class<?>> entry = this.handleScanned(binder, binds, bindAnnotation);
            bindings.put(entry.getKey(), entry.getValue());
        }

        Collection<Class<?>> multiBinders = Reflect.annotatedTypes(prefix, MultiBinds.class);
        for (Class<?> binder : multiBinders) {
            MultiBinds bindAnnotation = binder.getAnnotation(MultiBinds.class);
            for (Binds annotation : bindAnnotation.value()) {
                Class<?> binds = annotation.value();
                Entry<Key<?>, Class<?>> entry = this.handleScanned(binder, binds, annotation);
                bindings.put(entry.getKey(), entry.getValue());
            }
        }
        return bindings;
    }

    private Map.Entry<Key<?>, Class<?>> handleScanned(Class<?> binder, Class<?> binds, Binds bindAnnotation) {
        BindingMeta meta = bindAnnotation.meta();
        Key<?> key;
        if (!"".equals(meta.value())) {
            key = Key.get(binds, meta);
        }
        else {
            key = Key.get(binds);
        }
        return Tuple.of(key, binder);
    }

    @Override
    public <T, I extends T> Exceptional<Class<I>> getStaticBinding(Class<T> type) {
        for (Entry<Key<?>, Binding<?>> binding : this.getAllBindings().entrySet()) {
            if (binding.getKey().getTypeLiteral().getRawType().equals(type)) {
                //noinspection unchecked
                return Exceptional.of((Class<I>) binding.getValue().getKey().getTypeLiteral().getRawType());
            }
        }
        return Exceptional.none();
    }
}
