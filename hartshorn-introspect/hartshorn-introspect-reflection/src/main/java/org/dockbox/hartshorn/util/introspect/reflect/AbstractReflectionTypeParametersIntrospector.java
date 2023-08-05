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

package org.dockbox.hartshorn.util.introspect.reflect;

import org.dockbox.hartshorn.util.CollectionUtilities;
import org.dockbox.hartshorn.util.introspect.TypeParametersIntrospector;
import org.dockbox.hartshorn.util.introspect.view.TypeParameterView;
import org.dockbox.hartshorn.util.introspect.view.TypeView;
import org.dockbox.hartshorn.util.option.Option;

import java.util.List;

public abstract class AbstractReflectionTypeParametersIntrospector implements TypeParametersIntrospector {

    private List<TypeParameterView> outputParameters;

    protected abstract TypeView<?> type();

    @Override
    public Option<TypeParameterView> atIndex(final int index) {
        final List<TypeParameterView> parameters = this.all();
        if (parameters.size() > index) return Option.of(parameters.get(index));
        return Option.empty();
    }

    @Override
    public List<TypeParameterView> resolveFor(final Class<?> fromParentType) {
        // TODO: Complex resolving, first needs .all() to be implemented
        return null;
    }

    @Override
    public int count() {
        return this.all().size();
    }

    @Override
    public Option<TypeView<?>> at(final int index) {
        return this.atIndex(index).flatMap(TypeParameterView::resolvedType);
    }

    @Override
    public List<TypeParameterView> all() {
        return CollectionUtilities.mergeList(this.allInput(), this.allOutput());
    }

    @Override
    public List<TypeParameterView> allOutput() {
        if (this.outputParameters == null) {
            final TypeView<?> genericSuperClass = this.type().genericSuperClass();
            final List<TypeParameterView> superInput = genericSuperClass.typeParameters().allInput();
            final List<TypeParameterView> interfacesInput = this.type().genericInterfaces().stream()
                    .flatMap(genericInterface -> genericInterface.typeParameters().allInput().stream())
                    .toList();

            this.outputParameters = CollectionUtilities.mergeList(superInput, interfacesInput);
        }
        return this.outputParameters;
    }
}
