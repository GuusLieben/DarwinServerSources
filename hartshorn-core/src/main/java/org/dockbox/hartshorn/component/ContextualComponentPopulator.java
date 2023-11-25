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

import java.util.Arrays;
import java.util.Collection;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.context.Context;
import org.dockbox.hartshorn.context.ContextCarrier;
import org.dockbox.hartshorn.context.ContextKey;
import org.dockbox.hartshorn.inject.Enable;
import org.dockbox.hartshorn.inject.Populate;
import org.dockbox.hartshorn.inject.Populate.Type;
import org.dockbox.hartshorn.inject.Required;
import org.dockbox.hartshorn.inject.binding.collection.ComponentCollection;
import org.dockbox.hartshorn.introspect.ViewContextAdapter;
import org.dockbox.hartshorn.proxy.ProxyManager;
import org.dockbox.hartshorn.proxy.ProxyOrchestrator;
import org.dockbox.hartshorn.util.Lazy;
import org.dockbox.hartshorn.util.StringUtilities;
import org.dockbox.hartshorn.util.introspect.convert.ConversionService;
import org.dockbox.hartshorn.util.introspect.util.ParameterLoadException;
import org.dockbox.hartshorn.util.introspect.view.AnnotatedElementView;
import org.dockbox.hartshorn.util.introspect.view.FieldView;
import org.dockbox.hartshorn.util.introspect.view.MethodView;
import org.dockbox.hartshorn.util.introspect.view.TypeParameterView;
import org.dockbox.hartshorn.util.introspect.view.TypeView;
import org.dockbox.hartshorn.util.option.Option;

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * A component populator that uses the {@link ApplicationContext} to populate components. By default, all
 * fields and methods annotated with {@link Inject} are populated. This behaviour can be changed by annotating
 * the type with {@link Populate}, and configuring the {@link Populate#value() population targets}.
 *
 * <p>If a field is a {@link Collection}, the collection is populated with all components of the collection's
 * generic type. Collection lookups are performed for {@link ComponentCollection}s, and will be converted to
 * the compatible collection type if required.
 *
 * @see Populate
 * @see ComponentCollection
 * @see Context
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 *
 * @deprecated Use {@link org.dockbox.hartshorn.component.populate.StrategyComponentPopulator} instead.
 */
@Deprecated(forRemoval = true, since = "0.5.0")
public class ContextualComponentPopulator implements ComponentPopulator, ContextCarrier {

    private final ApplicationContext applicationContext;
    private final Lazy<ViewContextAdapter> adapter;
    private final Lazy<ConversionService> conversionService;

    public ContextualComponentPopulator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.adapter = Lazy.of(applicationContext, ViewContextAdapter.class);
        this.conversionService = Lazy.of(applicationContext, ConversionService.class);
    }

    @Override
    public <T> T populate(T instance) {
        if (null != instance) {
            T modifiableInstance = instance;
            ProxyOrchestrator orchestrator = this.applicationContext().environment().proxyOrchestrator();
            if (orchestrator.isProxy(instance)) {
                modifiableInstance = orchestrator
                        .manager(instance)
                        .flatMap(ProxyManager::delegate)
                        .orElse(modifiableInstance);
            }
            TypeView<T> typeView = this.applicationContext.environment().introspector().introspect(modifiableInstance);
            Option<Populate> populate = typeView.annotations().get(Populate.class);
            if (populate.absent() || shouldPopulateFields(populate.get())) {
                this.populateFields(typeView, modifiableInstance);
            }

            if (populate.absent() || shouldPopulateMethods(populate.get())) {
                this.populateMethods(typeView, modifiableInstance);
            }
        }
        return instance;
    }

    private static boolean shouldPopulateFields(Populate populate) {
        if (populate.fields()) {
            return true;
        }
        return Arrays.asList(populate.value()).contains(Type.FIELDS);
    }

    private static boolean shouldPopulateMethods(Populate populate) {
        if (populate.executables()) {
            return true;
        }
        return Arrays.asList(populate.value()).contains(Type.EXECUTABLES);
    }

    private <T> void populateMethods(TypeView<T> type, T instance) {
        for (MethodView<T, ?> method : type.methods().annotatedWith(Inject.class)) {
            try {
                Object[] arguments = this.adapter.get().loadParameters(method);
                method.invoke(instance, arguments).rethrow();
            }
            catch (ParameterLoadException e) {
                boolean required = this.isComponentRequired(e.parameter());

                if (required) {
                    String message = "Failed to populate method %s, parameter %s is required but not present in context"
                            .formatted(method.name(), e.parameter().name());
                    throw new ComponentRequiredException(message, e);
                }
                else {
                    this.applicationContext().log().warn("Failed to populate method {}, parameter {} is not present in context", method.name(), e.parameter().name());
                }
            }
            catch (Throwable t) {
                String message = "Failed to populate method %s, an exception occurred while populating the method"
                        .formatted(method.name());
                throw new ComponentPopulateException(message, t);
            }
        }
    }

    private <T> void populateFields(TypeView<T> type, T instance) {
        for (FieldView<T, ?> field : type.fields().annotatedWith(Inject.class)) {
            if (field.type().isChildOf(Collection.class)) {
                this.populateBeanCollectionField(type, instance, field);
            }
            else {
                this.populateObjectField(type, instance, field);
            }
        }
        for (FieldView<T, ?> field : type.fields().annotatedWith(org.dockbox.hartshorn.inject.Context.class)) {
            this.populateContextField(field, instance);
        }
    }

    private <T> void populateObjectField(TypeView<T> type, T instance, FieldView<T, ?> field) {
        ComponentKey<?> fieldKey = ComponentKey.of(field.type().type());
        if (field.annotations().has(Named.class)) {
            fieldKey = fieldKey.mutable().name(field.annotations().get(Named.class).get()).build();
        }

        Option<Enable> enableAnnotation = field.annotations().get(Enable.class);
        boolean enable = !enableAnnotation.present() || enableAnnotation.get().value();

        ComponentKey<?> componentKey = fieldKey.mutable().enable(enable).build();

        boolean required = this.isComponentRequired(field);

        Object fieldInstance;
        try {
            //Failing because DependencyGraph doesn't recognize ArgumentConverterRegistry as root due to self-dependency in binding method.
            fieldInstance = this.applicationContext().get(componentKey);
        }
        catch (ComponentResolutionException e) {
            if (required) {
                throw new ComponentRequiredException("Field '" + field.name() + "' in " + type.qualifiedName() + " is required", e);
            }
            else {
                this.applicationContext().handle("Failed to resolve component for field '" + field.name() + "' in type " + type.name(), e);
                return;
            }
        }

        this.applicationContext().log().debug("Injecting object of type {} into field {}", field.type().name(), field.qualifiedName());
        field.set(instance, fieldInstance);
    }

    private <T> void populateBeanCollectionField(TypeView<T> type, T instance, FieldView<T, ?> field) {
        Option<TypeView<?>> beanType = field.genericType()
                .typeParameters()
                .resolveInputFor(Collection.class)
                .atIndex(0)
                .flatMap(TypeParameterView::resolvedType);

        if (beanType.absent()) {
            throw new ComponentPopulateException("Failed to populate field " + field.name() + " in " + type.qualifiedName() + ", could not resolve bean type", null);
        }

        ComponentKey<? extends ComponentCollection<?>> beanKey = ComponentKey.collect(beanType.get().type());
        if (field.annotations().has(Named.class)) {
            beanKey = beanKey.mutable().name(field.annotations().get(Named.class).get()).build();
        }

        ComponentCollection<?> collection = this.applicationContext.get(beanKey);
        //noinspection unchecked
        Collection<Object> fieldValue = field.get(instance)
                .cast(Collection.class)
                .orCompute(() -> (Collection<Object>) this.conversionService.get().convert(null, field.type().type()))
                .get();
        fieldValue.addAll(collection);

        this.applicationContext().log().debug("Injecting bean collection of type {} into field {}", field.type().name(), field.qualifiedName());
        field.set(instance, fieldValue);
    }

    protected <T> void populateContextField(FieldView<T, ?> field, T instance) {
        TypeView<?> type = field.type();
        org.dockbox.hartshorn.inject.Context annotation = field.annotations().get(org.dockbox.hartshorn.inject.Context.class).get();

        if (!type.isChildOf(Context.class)) {
            throw new IllegalStateException("Field " + field.name() + " in " + field.declaredBy().name() + " is annotated with @Context but is not a Context");
        }
        ContextKey<? extends Context> contextKey = ContextKey.of((TypeView<? extends Context>) type);
        if (StringUtilities.notEmpty(annotation.value())) {
            contextKey = contextKey.mutable().name(annotation.value()).build();
        }
        Option<? extends Context> context = this.applicationContext().first(contextKey);

        boolean required = this.isComponentRequired(field);
        if (required && context.absent()) {
            throw new ComponentRequiredException("Context field " + field.name() + " in " + type.qualifiedName() + " is required, but not present in context");
        }

        this.applicationContext().log().debug("Injecting context of type {} into field {}", type, field.name());
        field.set(instance, context.orNull());
    }

    private boolean isComponentRequired(AnnotatedElementView view) {
        return Boolean.TRUE.equals(view.annotations().get(Required.class)
                .map(Required::value)
                .orElse(false));
    }

    @Override
    public ApplicationContext applicationContext() {
        return this.applicationContext;
    }
}
