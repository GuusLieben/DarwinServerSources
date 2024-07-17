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

package org.dockbox.hartshorn.inject.targets;

import org.dockbox.hartshorn.inject.populate.PopulateComponentContext;
import org.dockbox.hartshorn.util.ApplicationException;
import org.dockbox.hartshorn.util.TypeUtils;
import org.dockbox.hartshorn.util.introspect.ElementAnnotationsIntrospector;
import org.dockbox.hartshorn.util.introspect.view.AnnotatedGenericTypeView;
import org.dockbox.hartshorn.util.introspect.view.FieldView;
import org.dockbox.hartshorn.util.option.Option;

import java.util.Collection;
import java.util.List;
import java.util.SequencedCollection;

/**
 * Represents a field that can be injected into inside a component. Additional handling is in place to
 * support merging {@link Collection}s into existing values, if applicable.
 *
 * @param <T> the type of the component that this injection point is for
 *
 * @since 0.6.0
 *
 * @author Guus Lieben
 */
public class ComponentFieldInjectionPoint<T> implements ComponentInjectionPoint<T> {

    private final FieldView<T, ?> field;

    public ComponentFieldInjectionPoint(FieldView<T, ?> field) {
        this.field = field;
    }

    @Override
    public void processObjects(PopulateComponentContext<T> context, List<Object> objectsToInject) throws ApplicationException {
        if (objectsToInject.size() == 1) {
            Object objectToInject = objectsToInject.getFirst();
            T instance = context.instance();
            try {
                if (objectToInject instanceof Collection<?> collection) {
                    Option<?> previousValue = this.field.get(instance);
                    if (previousValue.present() && previousValue.get() instanceof Collection<?> existingCollection) {
                        existingCollection.addAll(TypeUtils.unchecked(collection, Collection.class));
                        return;
                    }
                }
                this.field.set(instance, objectToInject);
            }
            catch (ApplicationException e) {
                throw e;
            }
            catch (Throwable throwable) {
                throw new ApplicationException(throwable);
            }
        }
        else {
            throw new UnsupportedOperationException("Cannot inject multiple objects into a single field");
        }
    }

    @Override
    public ElementAnnotationsIntrospector annotations() {
        return this.field.annotations();
    }

    @Override
    public SequencedCollection<InjectionPoint> injectionPoints() {
        return List.of(new InjectionPoint(this.field));
    }

    @Override
    public String qualifiedName() {
        return this.field.name();
    }

    @Override
    public AnnotatedGenericTypeView<?> declaration() {
        return this.field;
    }
}
