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

import org.dockbox.hartshorn.inject.annotations.Named;
import org.dockbox.hartshorn.util.TypeUtils;
import org.dockbox.hartshorn.util.stream.EntryStream;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link QualifierKey} is a key that can be used to qualify a {@link ComponentKey}. It is a combination of a type
 * and a set of meta data. The type is used to identify the qualifier, while the meta data is used to provide further
 * information about the qualifier. For example, a {@link Named} qualifier can be used to identify a component, but
 * the name of the component is provided as meta data.
 *
 * <p>Meta data is always constrained to the type of the qualifier, meaning that the meta data can only contain keys
 * that are also present as methods on the qualifier type. For example, a {@link Named} qualifier can only contain
 * the key {@code value}, as that is the only method on the {@link Named} annotation.
 *
 * @param type The type of the qualifier.
 * @param meta The meta data of the qualifier.
 * @param <T> The type of the qualifier.
 *
 * @since 0.6.0
 *
 * @author Guus Lieben
 */
public record QualifierKey<T>(Class<T> type, Map<String, Object> meta) {

    /**
     * Creates a new {@link QualifierKey} instance based on the provided annotation. The type of the qualifier is
     * determined by the type of the annotation. The meta data is determined by the values of the annotation, excluding
     * any {@link Deprecated deprecated} members.
     *
     * <p><b>Note</b>: If possible, it is preferred to use {@link #of(Class, Map)} instead, as that method does not
     * require the annotation to be introspected at runtime.
     *
     * @param annotation The annotation to use as a qualifier.
     * @param <T> The type of the annotation.
     *
     * @return The new {@link QualifierKey} instance.
     */
    public static <T extends Annotation> QualifierKey<T> of(T annotation) {
        Class<T> annotationType = TypeUtils.unchecked(annotation.annotationType(), Class.class);
        Map<String, Object> annotationValues = TypeUtils.getAttributes(annotation);
        return of(annotationType, annotationValues);
    }

    /**
     * Creates a new {@link QualifierKey} instance based on the provided type and meta data. The type of the qualifier
     * is determined by the provided type. The meta data is determined by the provided meta data.
     *
     * @param type The type of the qualifier.
     * @param meta The meta data of the qualifier.
     * @param <T> The type of the qualifier.
     *
     * @return The new {@link QualifierKey} instance.
     */
    public static <T extends Annotation> QualifierKey<T> of(Class<T> type, Map<String, Object> meta) {
        checkValidMetadata(type, meta);
        return new QualifierKey<>(type, meta);
    }

    /**
     * Creates a new {@link QualifierKey} instance based on the provided type. It is expected that the provided
     * type does not have any attributes.
     *
     * @param type The type of the qualifier.
     * @param <T> The type of the qualifier.
     *
     * @return The new {@link QualifierKey} instance.
     */
    public static <T extends Annotation> QualifierKey<T> of(Class<T> type) {
        return of(type, Map.of());
    }

    /**
     * Creates a new {@link QualifierKey} instance based on the provided string. This method is a convenience method
     * when working with {@link Named} qualifiers.
     *
     * @param qualifier The name of the qualifier.
     * @return The new {@link QualifierKey} instance.
     */
    public static QualifierKey<Named> of(String qualifier) {
        return of(Named.class, Map.of("value", qualifier));
    }

    private static <T> void checkValidMetadata(Class<T> type, Map<String, Object> meta) {
        Set<String> methodNames = Arrays.stream(type.getDeclaredMethods())
                .filter(method -> !method.isAnnotationPresent(Deprecated.class))
                .map(Method::getName)
                .collect(Collectors.toSet());

        if (methodNames.size() != meta.size()) {
            throw new IllegalArgumentException("Meta size does not match method count on type '" + type.getSimpleName() + "'");
        }

        for (String metaName : meta.keySet()) {
            if (!methodNames.contains(metaName)) {
                throw new IllegalArgumentException("Meta key '" + metaName + "' is not a valid method name on type '" + type.getSimpleName() + "'");
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        QualifierKey<?> qualifierKey = (QualifierKey<?>) o;
        return Objects.equals(this.type, qualifierKey.type) && Objects.equals(this.meta, qualifierKey.meta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.meta);
    }

    @Override
    public String toString() {
        String metaString = EntryStream.of(this.meta)
                .map((key, value) -> "%s=%s".formatted(key, value))
                .collect(Collectors.joining(", "));
        return "%s{%s}".formatted(this.type.getSimpleName(), metaString);
    }
}
