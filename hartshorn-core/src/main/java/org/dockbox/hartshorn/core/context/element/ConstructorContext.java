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

package org.dockbox.hartshorn.core.context.element;

import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.exceptions.CyclicComponentException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.function.Function;

import lombok.Getter;

public final class ConstructorContext<T> extends ExecutableElementContext<Constructor<T>, T> implements TypedElementContext<T> {

    @Getter
    private final Constructor<T> constructor;
    private Function<Object[], Exceptional<T>> invoker;

    private ConstructorContext(final Constructor<T> constructor) {
        this.constructor = constructor;
        this.constructor.setAccessible(true);
    }

    public static <T> ConstructorContext<T> of(final Constructor<T> constructor) {
        return new ConstructorContext<>(constructor);
    }

    public Exceptional<T> createInstance(final Object... args) {
        this.prepareHandle();
        return this.invoker.apply(args);
    }

    public Exceptional<T> createInstance(final ApplicationContext context) {
        this.prepareHandle();
        try {
            final Object[] args = this.arguments(context);
            return this.invoker.apply(args);
        } catch (final StackOverflowError e) {
            // When the stack overflows, it typically indicates that there is a cycling dependency between the current component and one of its dependencies.
            // To provide a more meaningful error message, we need to find the cycle and provide a more specific error message. We do this by traversing the
            // dependency graph to find the cycle. If there is only one dependency, we can skip traversing and directly yield that as the cycling dependency.
            // TODO: This is a very naive implementation, and it is possible that there are more than one cycle. In that case, we need to find the longest
            //       cycle and provide a more specific error message.
            final LinkedList<TypeContext<?>> parameterTypes = this.parameterTypes();
            if (parameterTypes.size() == 1) {
                return Exceptional.of(new CyclicComponentException(this, parameterTypes.get(0)));
            }
            else {
                for (final TypeContext<?> parameterType : parameterTypes) {
                    for (final ConstructorContext<?> constructor : parameterType.injectConstructors()) {
                        if (constructor.parameterTypes().contains(this.type())) {
                            return Exceptional.of(new CyclicComponentException(this, parameterType));
                        }
                    }
                }
                return Exceptional.of(new CyclicComponentException(this, null));
            }
        }
    }

    @Override
    public TypeContext<T> type() {
        return this.parent();
    }

    @Override
    public String name() {
        return this.qualifiedName();
    }

    private void prepareHandle() {
        if (this.invoker == null) {
            this.invoker = args -> Exceptional.of(() -> {
                try {
                    return this.constructor.newInstance(args);
                } catch (final InvocationTargetException e) {
                    if (e.getCause() instanceof Exception ex) throw ex;
                    throw e;
                }
            });
        }
    }

    @Override
    protected Constructor<T> element() {
        return this.constructor();
    }

    @Override
    public String qualifiedName() {
        return "Constructor[%s]".formatted(this.type().qualifiedName());
    }
}
