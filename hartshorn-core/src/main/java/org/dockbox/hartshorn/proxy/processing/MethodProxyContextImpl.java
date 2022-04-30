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

package org.dockbox.hartshorn.proxy.processing;

import org.dockbox.hartshorn.context.DefaultContext;
import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.util.reflect.MethodContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;

import java.lang.annotation.Annotation;

public class MethodProxyContextImpl<T> extends DefaultContext implements MethodProxyContext<T> {

    private final ApplicationContext context;
    private final TypeContext<T> type;
    private final MethodContext<?, T> method;

    public MethodProxyContextImpl(final ApplicationContext context, final TypeContext<T> type, final MethodContext<?, T> method) {
        this.context = context;
        this.type = type;
        this.method = method;
    }

    @Override
    public <A extends Annotation> A annotation(final Class<A> annotation) {
        return this.method.annotation(annotation).orNull();
    }

    @Override
    public ApplicationContext context() {
        return this.context;
    }

    @Override
    public TypeContext<T> type() {
        return this.type;
    }

    @Override
    public MethodContext<?, T> method() {
        return this.method;
    }
}