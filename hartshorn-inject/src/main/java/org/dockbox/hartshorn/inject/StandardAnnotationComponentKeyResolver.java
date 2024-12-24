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

package org.dockbox.hartshorn.inject;

import org.dockbox.hartshorn.inject.annotations.Initialize;
import org.dockbox.hartshorn.inject.annotations.Priority;
import org.dockbox.hartshorn.inject.annotations.Qualifier;
import org.dockbox.hartshorn.inject.annotations.Strict;
import org.dockbox.hartshorn.inject.populate.ComponentPopulateException;
import org.dockbox.hartshorn.inject.provider.selection.ExactPriorityProviderSelectionStrategy;
import org.dockbox.hartshorn.inject.provider.selection.MaximumPriorityProviderSelectionStrategy;
import org.dockbox.hartshorn.inject.scope.Scope;
import org.dockbox.hartshorn.util.Tristate;
import org.dockbox.hartshorn.util.introspect.ElementAnnotationsIntrospector;
import org.dockbox.hartshorn.util.introspect.view.AnnotatedGenericTypeView;
import org.dockbox.hartshorn.util.introspect.view.ExecutableElementView;
import org.dockbox.hartshorn.util.introspect.view.ParameterView;
import org.dockbox.hartshorn.util.introspect.view.TypeParameterView;
import org.dockbox.hartshorn.util.introspect.view.TypeView;
import org.dockbox.hartshorn.util.option.Option;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link ComponentKeyResolver} that resolves the {@link ComponentKey} of a component based on the
 * presence of annotations on the component type. The key is resolved as follows:
 * <ul>
 *     <li>The type of the component is used as the type of the key</li>
 *     <li>The scope of the key is set to the given scope</li>
 *     <li>The qualifiers of the key are set to the meta data of {@link Qualifier meta qualifiers} on the component type</li>
 *     <li>The strictness of the key is set to the value of the {@link Strict} annotation on the component type</li>
 *     <li>The auto-enabling of the key is set to the value of the {@link Initialize} annotation on the component type</li>
 *     <li>The priority of the key is set to the value of the {@link Priority} annotation on the component type,
 *     or configured based on the declaring element if applicable</li>
 *     <li>If the component type is a {@link Collection}, the key is treated as a collector and the element type of the
 *     collection is used as the type of the key</li>
 * </ul>
 *
 * @since 0.6.0
 *
 * @author Guus Lieben
 */
public class StandardAnnotationComponentKeyResolver implements ComponentKeyResolver {

    @Override
    public ComponentKey<?> resolve(AnnotatedGenericTypeView<?> view, Scope scope) {
        TypeView<?> type = view.genericType();
        ComponentKey.Builder<?> builder = ComponentKey.builder(type).scope(scope);

        ElementAnnotationsIntrospector annotations = view.annotations();
        this.configureQualifiers(builder, annotations);
        this.configureStrict(builder, annotations);
        this.configureAutoEnabling(builder, annotations);
        this.configurePriority(builder, view, scope);

        return this.postConfigureKey(builder, view);
    }

    /**
     * Post configures the given {@link ComponentKey.Builder} based on the given {@link AnnotatedGenericTypeView}. This
     * usually entails that final details are added- or transformations are applied to the key, such turning the key
     * into a collector if the type is a {@link Collection}.
     *
     * @param builder the builder to post configure
     * @param view the view to post configure the builder with
     * @return the post configured key
     */
    protected ComponentKey<?> postConfigureKey(ComponentKey.Builder<?> builder, AnnotatedGenericTypeView<?> view) {
        TypeView<?> type = view.genericType();
        if (type.isChildOf(Collection.class)) {
            TypeView<?> elementType = this.resolveCollectionElementType(type);
            return builder.type(elementType).collector().build();
        }
        else {
            return builder.build();
        }
    }

    /**
     * Configures the qualifiers of the given {@link ComponentKey.Builder} based on available metadata, typically in the
     * form of {@link Qualifier} annotations.
     *
     * @param builder the builder to configure
     * @param annotations the annotations to configure the builder with
     */
    protected void configureQualifiers(ComponentKey.Builder<?> builder, ElementAnnotationsIntrospector annotations) {
        Set<QualifierKey<?>> qualifiers = this.resolveQualifiers(annotations);
        builder.qualifiers(qualifiers);
    }

    /**
     * Resolves the qualifiers of the given {@link ComponentKey.Builder} based on available metadata, typically in the
     * form of {@link Qualifier} annotations. The result may include any amount of {@link QualifierKey}s, though keys
     * may apply restrictions to the presence of duplicate {@link QualifierKey#type() qualifier types}.
     *
     * @param annotations the annotations to resolve the qualifiers from
     * @return the resolved qualifiers
     */
    protected Set<QualifierKey<?>> resolveQualifiers(ElementAnnotationsIntrospector annotations) {
        return annotations.annotedWith(Qualifier.class).stream()
                .map(QualifierKey::of)
                .collect(Collectors.toSet());
    }

    /**
     * Configures the strictness of the given {@link ComponentKey.Builder} based on available metadata.
     *
     * @param builder the builder to configure
     * @param annotations the annotations to configure the builder with
     */
    protected void configureStrict(ComponentKey.Builder<?> builder, ElementAnnotationsIntrospector annotations) {
        Tristate isStrict = this.isStrict(annotations);
        if (isStrict != Tristate.UNDEFINED) {
            builder.strict(isStrict.booleanValue());
        }
    }

    /**
     * Determines the strictness of the given {@link ComponentKey.Builder} based on available metadata. If the
     * strictness cannot be determined, {@link Tristate#UNDEFINED} is returned.
     *
     * @param annotations the annotations to determine the strictness from
     * @return the strictness of the key, or {@link Tristate#UNDEFINED} if it cannot be determined
     */
    protected Tristate isStrict(ElementAnnotationsIntrospector annotations) {
        return annotations.get(Strict.class).map(Strict::value)
                .map(Tristate::valueOf)
                .orElse(Tristate.UNDEFINED);
    }

    /**
     * Configures the auto-enabling of the given {@link ComponentKey.Builder} based on available metadata.
     *
     * @param builder the builder to configure
     * @param annotations the annotations to configure the builder with
     */
    protected void configureAutoEnabling(ComponentKey.Builder<?> builder, ElementAnnotationsIntrospector annotations) {
        builder.postConstructionAllowed(this.isAutoEnabled(annotations));
    }

    /**
     * Determines the auto-enabling of the given {@link ComponentKey.Builder} based on available metadata. If the
     * auto-enabling cannot be determined, {@code true} is returned.
     *
     * @param annotations the annotations to determine the auto-enabling from
     * @return the auto-enabling of the key, or {@code true} if it cannot be determined
     */
    protected boolean isAutoEnabled(ElementAnnotationsIntrospector annotations) {
        return annotations.get(Initialize.class).map(Initialize::value).orElse(true);
    }

    /**
     * Configures the priority of the given {@link ComponentKey.Builder} based on available metadata.
     *
     * @param builder the builder to configure
     * @param view the view to configure the builder with
     * @param scope the scope of the key
     */
    protected void configurePriority(ComponentKey.Builder<?> builder, AnnotatedGenericTypeView<?> view, Scope scope) {
        Option<Priority> priorityOption = view.annotations().get(Priority.class);
        if (priorityOption.present()) {
            int parameterPriority = priorityOption.get().value();
            builder.strategy(new ExactPriorityProviderSelectionStrategy(parameterPriority));
        }
        else if(view instanceof ParameterView<?> parameterView) {
            this.configureSelfProvisionCandidate(builder, scope, parameterView);
        }
    }

    /**
     * Configures the priority of the given {@link ComponentKey.Builder} based on provided {@link ParameterView}. If
     * the parameter is declared by a method which declares a binding of the same type as the parameter, the priority
     * of the key is set to be at most the priority of the binding (exclusive).
     *
     * @param builder the builder to configure
     * @param scope the scope of the key
     * @param parameterView the parameter to configure the builder with
     */
    protected void configureSelfProvisionCandidate(ComponentKey.Builder<?> builder, Scope scope, ParameterView<?> parameterView) {
        ExecutableElementView<?> declaredBy = parameterView.declaredBy();
        if (declaredBy instanceof AnnotatedGenericTypeView<?> annotatedDeclaredBy) {
            ComponentKey<?> key = this.resolve(annotatedDeclaredBy, scope);
            boolean selfProvision = key.view().matches(builder.view());
            if (selfProvision) {
                Option<Priority> priority = declaredBy.annotations().get(Priority.class);
                if (priority.present()) {
                    builder.strategy(new MaximumPriorityProviderSelectionStrategy(priority.get().value()));
                }
            }
        }
    }

    /**
     * Resolves the element type of the given {@link Collection} injection point. If the element type
     * cannot be resolved, an exception is thrown.
     *
     * @param type the injection point to resolve the element type for
     * @return the element type
     *
     * @throws ComponentPopulateException if the element type cannot be resolved
     */
    protected TypeView<?> resolveCollectionElementType(TypeView<?> type) {
        Option<TypeView<?>> elementType = type
                .typeParameters()
                .inputFor(Collection.class)
                .atIndex(0)
                .flatMap(TypeParameterView::resolvedType);

        if (elementType.absent()) {
            throw new ComponentPopulateException("Failed to populate injection point " + type.qualifiedName() + ", could not resolve collection element type");
        }
        return elementType.get();
    }
}
