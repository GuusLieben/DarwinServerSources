/*
 * Copyright 2019-2022 the original author or authors.
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

package org.dockbox.hartshorn.util.introspect.view;

import org.dockbox.hartshorn.util.option.FailableOption;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

public interface MethodView<Parent, ReturnType> extends ExecutableElementView<Parent>, AnnotatedElementView, GenericTypeView<ReturnType>, ObtainableView<ReturnType> {

    Method method();

    default FailableOption<ReturnType, Throwable> invoke(final Parent instance, final Object... arguments) {
        return this.invoke(instance, Arrays.asList(arguments));
    }

    FailableOption<ReturnType, Throwable> invoke(Parent instance, Collection<?> arguments);

    FailableOption<ReturnType, Throwable> invokeWithContext(Parent instance);

    default FailableOption<ReturnType, Throwable> invokeWithContext(final Object... arguments) {
        return this.invokeWithContext(Arrays.asList(arguments));
    }

    FailableOption<ReturnType, Throwable> invokeWithContext(Collection<?> arguments);

    FailableOption<ReturnType, Throwable> invokeWithContext();

    default FailableOption<ReturnType, Throwable> invokeStatic(final Object... arguments) {
        return this.invokeStatic(Arrays.asList(arguments));
    }

    FailableOption<ReturnType, Throwable> invokeStatic(Collection<?> arguments);

    FailableOption<ReturnType, Throwable> invokeStaticWithContext();

    TypeView<ReturnType> returnType();

    TypeView<ReturnType> genericReturnType();

    boolean isProtected();

    boolean isPublic();

    boolean isPrivate();

    boolean isStatic();

    boolean isFinal();

    boolean isAbstract();

    boolean isDefault();

}