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

package org.dockbox.hartshorn.util.introspect.reflect.view;

import org.dockbox.hartshorn.util.introspect.ElementModifiersIntrospector;
import org.dockbox.hartshorn.util.introspect.ExecutableParametersIntrospector;
import org.dockbox.hartshorn.util.introspect.IllegalIntrospectionException;
import org.dockbox.hartshorn.util.introspect.Introspector;
import org.dockbox.hartshorn.util.introspect.TypeVariablesIntrospector;
import org.dockbox.hartshorn.util.introspect.reflect.ReflectionElementModifiersIntrospector;
import org.dockbox.hartshorn.util.introspect.reflect.ReflectionExecutableParametersIntrospector;
import org.dockbox.hartshorn.util.introspect.reflect.ReflectionIntrospector;
import org.dockbox.hartshorn.util.introspect.reflect.ReflectionTypeVariablesIntrospector;
import org.dockbox.hartshorn.util.introspect.view.ExecutableElementView;
import org.dockbox.hartshorn.util.introspect.view.TypeView;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.util.List;

public abstract class ReflectionExecutableElementView<Parent, ResultType> extends ReflectionAnnotatedElementView implements ExecutableElementView<Parent> {

    private final Introspector introspector;
    private final Executable executable;

    private ExecutableParametersIntrospector parametersIntrospector;
    private TypeVariablesIntrospector typeVariablesIntrospector;

    public ReflectionExecutableElementView(final ReflectionIntrospector introspector, final Executable executable) {
        super(introspector);
        this.executable = executable;
        this.introspector = introspector;
        if (!executable.trySetAccessible()) {
            final String packageName = executable.getDeclaringClass().getPackageName();
            if (!(packageName.startsWith("java.") || packageName.startsWith("jdk.") || packageName.startsWith("sun.") || packageName.startsWith("com.sun.") || packageName.startsWith("javax."))) {
                throw new IllegalIntrospectionException(executable, "Unable to set executable " + executable.getName() + " accessible");
            }
        }
    }

    public Executable executable() {
        return this.executable;
    }

    @Override
    public ExecutableParametersIntrospector parameters() {
        if (this.parametersIntrospector == null) {
            this.parametersIntrospector = new ReflectionExecutableParametersIntrospector(this.introspector, this);
        }
        return this.parametersIntrospector;
    }

    @Override
    public TypeVariablesIntrospector typeVariables() {
        if (this.typeVariablesIntrospector == null) {
            this.typeVariablesIntrospector = new ReflectionTypeVariablesIntrospector(this.introspector, List.of(this.executable.getTypeParameters()));
        }
        return this.typeVariablesIntrospector;
    }

    @Override
    public TypeView<Parent> declaredBy() {
        return (TypeView<Parent>) this.introspector.introspect(this.executable.getDeclaringClass());
    }

    @Override
    protected AnnotatedElement annotatedElement() {
        return this.executable;
    }

    @Override
    public ElementModifiersIntrospector modifiers() {
        return new ReflectionElementModifiersIntrospector(this.executable);
    }
}
