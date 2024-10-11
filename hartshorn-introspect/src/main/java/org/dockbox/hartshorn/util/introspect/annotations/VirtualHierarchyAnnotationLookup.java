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

package org.dockbox.hartshorn.util.introspect.annotations;

import org.dockbox.hartshorn.util.option.Option;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * An {@link AnnotationLookup} that uses a virtual annotation hierarchy to resolve annotations.
 * Virtual annotations are annotations that are not declared on the element itself, but are
 * inherited from a meta annotation. This lookup will resolve virtual annotations by creating
 * proxies that implement the requested annotation interface, and delegate to the actual annotation
 * instance or meta annotations to resolve the value.
 *
 * <p>For example, consider the following annotations:
 * <pre>{@code
 * @interface MetaAnnotation {
 *    String name();
 * }
 *
 * @Extends(MetaAnnotation.class)
 * @MetaAnnotation(name = "meta")
 * @interface VirtualAnnotation {
 *   String id();
 * }
 * }</pre>
 *
 * <p>When an element is annotated with {@code @VirtualAnnotation(id = "foo")}, and a request is
 * made to find the {@code MetaAnnotation} on that element, this lookup will return a proxy
 * implementation of {@code MetaAnnotation} that delegates to the {@code VirtualAnnotation} on the
 * element, and the {@code MetaAnnotation} on the {@code VirtualAnnotation} to resolve the value
 * for requested attributes. In this example, the {@code name} attribute will be resolved to
 * {@code "meta"}, and the {@code id} attribute will be resolved to {@code "foo"}.
 *
 * @since 0.4.13
 *
 * @author Guus Lieben
 */
public class VirtualHierarchyAnnotationLookup implements AnnotationLookup {

    private static final Map<HierarchyKey, Option<Object>> cache = new ConcurrentHashMap<>();

    @Override
    public <A extends Annotation> A find(AnnotatedElement element, Class<A> annotationType) throws DuplicateAnnotationCompositeException {
        List<A> allInHierarchy = this.findAll(element, annotationType);
        if (allInHierarchy.size() > 1) {
            throw new DuplicateAnnotationCompositeException(element, allInHierarchy);
        }
        return allInHierarchy.isEmpty() ? null : allInHierarchy.getFirst();
    }

    @Override
    public <A extends Annotation> List<A> findAll(AnnotatedElement element, Class<A> annotationType) {
        HierarchyKey key = new HierarchyKey(element, annotationType);
        return this.fromCache(key, () -> this.annotationsOnElement(element, annotationType));
    }

    protected <T> T fromCache(HierarchyKey key, Supplier<T> supplier) {
        Option<Object> ret = cache.get(key);
        if (ret == null) {
            ret = Option.of(supplier::get);
            cache.put(key, ret);
        }
        return (T) ret.get();
    }

    /**
     * Returns all annotations compatible with the given annotation type on the given element. This
     * includes the annotation itself, and all annotations that are annotated with the given
     * annotation type. Annotations that are annotated with the given annotation type are returned
     * as a proxy that implements the given annotation type, and delegates to the actual annotation
     * to resolve the value for requested attributes.
     *
     * @param element The element to find the annotations on
     * @param annotationType The type of the annotations to find
     * @param <A> The type of the annotation
     *
     * @return All annotations compatible with the given annotation type on the given element
     */
    protected <A extends Annotation> List<A> annotationsOnElement(AnnotatedElement element, Class<A> annotationType) {
        return Arrays.stream(element.getAnnotations())
                .map(annotation -> this.examineAnnotation(annotation, annotationType))
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Examines the given annotation, and returns a proxy that implements the given annotation type
     * if the given annotation is annotated with the given annotation type. If the given annotation
     * is not annotated with the given annotation type, {@code null} is returned.
     *
     * @param actual The annotation to examine
     * @param targetAnnotationClass The annotation type to find
     * @param <A> The type of the annotation
     *
     * @return A proxy that implements the given annotation type, or {@code null}
     */
    protected <A extends Annotation> A examineAnnotation(Annotation actual, Class<A> targetAnnotationClass) {
        actual = this.unproxy(actual);
        SequencedSet<Class<? extends Annotation>> hierarchy = this.annotationHierarchy(actual.annotationType());

        if (!hierarchy.contains(targetAnnotationClass)) {
            return null;
        }

        InvocationHandler adapter = new AnnotationAdapterProxy<>(actual, targetAnnotationClass, hierarchy, this);
        HashSet<Class<?>> parentInterfaces = new HashSet<>(hierarchy);
        parentInterfaces.add(AnnotationAdapter.class);

        Class<?>[] interfaces = { targetAnnotationClass, AnnotationAdapter.class };
        Object proxy = Proxy.newProxyInstance(
                VirtualHierarchyAnnotationLookup.class.getClassLoader(),
                parentInterfaces.toArray(Class[]::new),
                adapter);

        return targetAnnotationClass.cast(proxy);
    }

    @Override
    public Annotation unproxy(Annotation annotation) {
        if (annotation instanceof AnnotationAdapter adapter) {
            return adapter.actualAnnotation();
        }
        return annotation;
    }

    @Override
    public SequencedSet<Class<? extends Annotation>> annotationHierarchy(Class<? extends Annotation> type) {
        Class<? extends Annotation> currentClass = type;
        SequencedSet<Class<? extends Annotation>> hierarchy = new LinkedHashSet<>();
        while (currentClass != null) {
            if (!hierarchy.add(currentClass)) {
                throw new CircularHierarchyException(currentClass);
            }
            currentClass = superAnnotationOrNull(currentClass);
        }

        return hierarchy;
    }

    private static Class<? extends Annotation> superAnnotationOrNull(Class<? extends Annotation> currentClass) {
        Extends extendsAnnotation = currentClass.getAnnotation(Extends.class);
        return extendsAnnotation == null ? null : extendsAnnotation.value();
    }
}
