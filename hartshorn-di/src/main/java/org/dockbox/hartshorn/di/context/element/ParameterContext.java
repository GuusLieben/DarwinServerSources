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

package org.dockbox.hartshorn.di.context.element;

import org.dockbox.hartshorn.util.HartshornUtils;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;

@SuppressWarnings("unchecked")
public class ParameterContext<T> extends AnnotatedElementContext<Parameter> {

    private final Parameter parameter;
    @Getter private final boolean isVarargs;

    private String name;
    private TypeContext<T> type;
    private List<TypeContext<?>> typeParameters;

    private ParameterContext(final Parameter parameter) {
        this.parameter = parameter;
        this.isVarargs = parameter.isVarArgs();
    }

    public static <T> ParameterContext<T> of(final Parameter parameter) {
        return new ParameterContext<>(parameter);
    }

    public String name() {
        if (this.name == null) {
            this.name = this.element().getName();
        }
        return this.name;
    }

    public TypeContext<T> type() {
        if (this.type == null) {
            this.type = TypeContext.of((Class<T>) this.element().getType());
        }
        return this.type;
    }

    @Override
    protected Parameter element() {
        return this.parameter;
    }

    public List<TypeContext<?>> typeParameters() {
        if (this.typeParameters == null) {
            final Type type = this.element().getParameterizedType();
            if (type instanceof ParameterizedType parameterized) {
                this.typeParameters = Arrays.stream(parameterized.getActualTypeArguments())
                        .map(t -> {
                            if (t instanceof WildcardType) return WildcardTypeContext.create();
                            else if (t instanceof Class<?> clazz) return TypeContext.of(clazz);
                            else return TypeContext.VOID;
                        })
                        .collect(Collectors.toList());
            }
            else {
                this.typeParameters = HartshornUtils.emptyList();
            }
        }
        return this.typeParameters;
    }
}