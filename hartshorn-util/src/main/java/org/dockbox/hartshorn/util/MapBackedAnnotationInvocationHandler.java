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

package org.dockbox.hartshorn.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * An {@link InvocationHandler} for an annotation which is backed by a {@link Map} of values.
 * This is used to create a dynamic proxy for an annotation which is not present at runtime, or
 * to create a virtual annotation which was never declared on a member.
 *
 * <p>Values are retrieved from the map, or from the default value of the method if the map does
 * not contain a value for the method name. Standard methods such as {@link Object#toString()},
 * {@link Object#hashCode()} and {@link Object#equals(Object)} are also supported. {@link Annotation#annotationType()}
 * will use the type provided in the constructor.
 *
 * @see TypeUtils#annotation(Class)
 * @see TypeUtils#annotation(Class, Map)
 *
 * @since 0.4.13
 *
 * @author Guus Lieben
 */
public class MapBackedAnnotationInvocationHandler implements InvocationHandler {

    private final Class<?> type;
    private final Map<String, Object> values;

    /**
     * Creates a new {@link MapBackedAnnotationInvocationHandler} instance. The values map is checked
     * for validity, and an exception is thrown if the values are not valid for the annotation type.
     *
     * @param type the type of the annotation
     * @param values the values of the annotation
     *
     * @throws IllegalStateException if the values are not valid for the annotation type
     */
    public MapBackedAnnotationInvocationHandler(Class<?> type, Map<String, Object> values) {
        this.type = type;
        this.values = this.checkValues(type, values);
    }

    /**
     * Checks if the values map is valid for the annotation type. This is done by checking if the keys
     * of the map are present as methods on the annotation type, and if the values are assignable to
     * the return type of the method. If the values map is null, or the type is null, an exception is
     * thrown.
     *
     * @param type the type of the annotation
     * @param values the values of the annotation
     * @return the values map
     *
     * @throws IllegalStateException if the values are not valid for the annotation type, or if either the
     *                               values map or the type are null
     */
    private Map<String, Object> checkValues(Class<?> type, Map<String, Object> values) {
        if (values == null) {
            throw new IllegalStateException("Values map is null");
        }
        if (type == null) {
            throw new IllegalStateException("Type is null");
        }
        CollectionUtilities.iterateEntries(values.entrySet(), (property, value) -> {
            try {
                Method method = type.getDeclaredMethod(property);
                if (!TypeUtils.isAssignable(method.getReturnType(), value.getClass())) {
                    throw new IllegalStateException("Value " + value + " is not assignable to " + method.getReturnType());
                }
            }
            catch (NoSuchMethodException e) {
                throw new IllegalStateException("No such method " + property + " on annotation " + type.getCanonicalName());
            }
        });
        return values;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if ("annotationType".equals(methodName)) {
            return this.type;
        }
        if ("toString".equals(methodName)) {
            return "@" + this.type;
        }
        if ("hashCode".equals(methodName)) {
            return this.values.hashCode();
        }
        if ("equals".equals(methodName)) {
            return proxy == args[0];
        }
        if (this.values.containsKey(methodName)) {
            return this.values.get(methodName);
        }
        else {
            return method.getDefaultValue();
        }
    }

    /**
     * Returns the type of the annotation.
     *
     * @return the type of the annotation
     */
    public Class<?> type() {
        return this.type;
    }
}
