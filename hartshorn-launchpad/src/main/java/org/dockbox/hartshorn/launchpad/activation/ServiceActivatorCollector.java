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

package org.dockbox.hartshorn.launchpad.activation;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Collects {@link ServiceActivator} annotations from a given class and its superclasses.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class ServiceActivatorCollector {

    public Set<Annotation> serviceActivators(Class<?> forClass) {
        return this.collectServiceActivatorsOnType(forClass)
            .stream()
            .flatMap(activator -> this.collectServiceActivatorsRecursively(activator).stream())
            .collect(Collectors.toSet());
    }

    public Set<Annotation> collectServiceActivatorsRecursively(Annotation annotation) {
        Set<Annotation> serviceActivatorsOnAnnotation = this.collectServiceActivatorsOnType(annotation.annotationType());
        Set<Annotation> activators = new HashSet<>(serviceActivatorsOnAnnotation);
        activators.add(annotation);

        for (Annotation activator : serviceActivatorsOnAnnotation) {
            activators.addAll(this.collectServiceActivatorsRecursively(activator));
        }
        return activators;
    }

    protected Set<Annotation> collectServiceActivatorsOnType(Class<?> type) {
        return Stream.of(type.getAnnotations())
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(ServiceActivator.class))
                .collect(Collectors.toSet());
    }

    public Set<ServiceActivator> collectDeclarationsOnActivator(Annotation annotation) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        Set<ServiceActivator> activators = new HashSet<>();
        if (annotationType.isAnnotationPresent(ServiceActivator.class)) {
            activators.add(annotationType.getAnnotation(ServiceActivator.class));
        }
        Arrays.stream(annotationType.getAnnotations())
                .filter(ann -> ann.annotationType().isAnnotationPresent(ServiceActivator.class))
                .map(ann -> ann.annotationType().getAnnotation(ServiceActivator.class))
                .forEach(activators::add);
        return activators;
    }
}
