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

package org.dockbox.hartshorn.util.introspect.reflect;

import org.dockbox.hartshorn.util.CollectionUtilities;
import org.dockbox.hartshorn.util.graph.Graph;
import org.dockbox.hartshorn.util.graph.GraphException;
import org.dockbox.hartshorn.util.graph.GraphInverter;
import org.dockbox.hartshorn.util.introspect.Introspector;
import org.dockbox.hartshorn.util.introspect.SimpleTypeParameterList;
import org.dockbox.hartshorn.util.introspect.TypeParameterList;
import org.dockbox.hartshorn.util.introspect.TypeParametersIntrospector;
import org.dockbox.hartshorn.util.introspect.reflect.view.TypeHierarchyGraph;
import org.dockbox.hartshorn.util.introspect.view.TypeParameterView;
import org.dockbox.hartshorn.util.introspect.view.TypeView;
import org.dockbox.hartshorn.util.option.Option;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A base implementation of {@link TypeParametersIntrospector} that uses reflection to introspect the type parameters of
 * a given {@link TypeView}. This implementation is shared between {@link Class classes} and {@link
 * java.lang.reflect.ParameterizedType parameterized types}, with only the {@link #allInput()} method being implemented
 * differently.
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public abstract class AbstractReflectionTypeParametersIntrospector implements TypeParametersIntrospector {

    private final TypeView<?> type;
    private final Introspector introspector;

    private final Map<Class<?>, TypeParameterList> resolvedParameters = new ConcurrentHashMap<>();

    private TypeHierarchyGraph typeHierarchy;
    private TypeParameterList outputParameters;

    protected AbstractReflectionTypeParametersIntrospector(TypeView<?> type, Introspector introspector) {
        this.type = type;
        this.introspector = introspector;
    }

    /**
     * Returns the type that is being introspected.
     *
     * @return the type that is being introspected
     */
    protected TypeView<?> type() {
        return this.type;
    }

    /**
     * Returns the introspector that is used to introspect the type, and can be used
     * to introspect any other types that are encountered during introspection.
     *
     * @return the introspector that is used to introspect the type
     */
    protected Introspector introspector() {
        return this.introspector;
    }

    @Override
    public Option<TypeParameterView> atIndex(int index) {
        TypeParameterList parameters = this.allInput();
        return parameters.atIndex(index);
    }

    @Override
    public TypeParameterList inputFor(Class<?> fromParentType) {
        if (this.type().is(fromParentType)) {
            return this.allInput();
        }
        else if (this.type().isChildOf(fromParentType)) {
            TypeView<?> parentType = this.introspector().introspect(fromParentType);
            TypeParametersIntrospector typeParameters = parentType.typeParameters();
            // No point in resolving if there are no type parameters
            if (!(typeParameters.allOutput().isEmpty() && typeParameters.allInput().isEmpty())) {
                return this.tryResolveInputForParent(parentType);
            }
        }
        return new SimpleTypeParameterList(List.of());
    }

    private TypeParameterList tryResolveInputForParent(TypeView<?> parent) {
        if (this.type.isChildOf(parent.type())) {
            return this.resolvedParameters.computeIfAbsent(parent.type(), parentType -> {
                TypeHierarchyGraph typeHierarchy = this.getTypeHierarchy();
                try {
                    Graph<TypeView<?>> inverted = new GraphInverter().invertGraph(typeHierarchy, node -> node.type() == parentType);
                    TypeParameterResolver visitor = new TypeParameterResolver(parent);
                    List<TypeParameterView> parameters = visitor.tryResolveFromGraph(inverted);
                    return new SimpleTypeParameterList(parameters);
                }
                catch (GraphException e) {
                    throw new IllegalStateException("Unexpected graph exception while inverting type hierarchy", e);
                }
                catch (TypeParameterResolutionException e) {
                    // TypeParameterResolverGraphVisitor doesn't throw any exceptions, so this should never happen. If it does,
                    // it indicates something was unexpectedly modified in the implementation.
                    throw new IllegalStateException("Unexpected graph exception while resolving type parameters", e);
                }
            });
        }
        else {
            return new SimpleTypeParameterList(List.of());
        }
    }

    private TypeHierarchyGraph getTypeHierarchy() {
        if (this.typeHierarchy == null) {
            this.typeHierarchy = TypeHierarchyGraph.of(this.type());
        }
        return this.typeHierarchy;
    }

    @Override
    public TypeParameterList allOutput() {
        if (this.outputParameters == null) {
            TypeView<?> genericSuperClass = this.type().genericSuperClass();
            List<TypeParameterView> superInput = genericSuperClass.typeParameters().allInput().asList();
            List<TypeParameterView> interfacesInput = this.type().genericInterfaces().stream()
                    .flatMap(genericInterface -> genericInterface.typeParameters().allInput().stream())
                    .toList();

            List<TypeParameterView> allInputParameters = CollectionUtilities.mergeList(superInput, interfacesInput);
            this.outputParameters = new SimpleTypeParameterList(allInputParameters);
        }
        return this.outputParameters;
    }

    @Override
    public TypeParameterList outputFor(Class<?> fromParentType) {
        if (this.type().isChildOf(fromParentType)) {
            List<TypeParameterView> consumedByParent = this.allOutput().stream()
                    .filter(parameter -> parameter.consumedBy().is(fromParentType))
                    .toList();
            return new SimpleTypeParameterList(consumedByParent);
        }
        return new SimpleTypeParameterList(List.of());
    }
}
