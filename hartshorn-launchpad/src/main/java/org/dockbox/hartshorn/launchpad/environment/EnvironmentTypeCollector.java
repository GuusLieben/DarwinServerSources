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

package org.dockbox.hartshorn.launchpad.environment;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.dockbox.hartshorn.launchpad.ApplicationContext;
import org.dockbox.hartshorn.util.introspect.scan.ClassReferenceLoadException;
import org.dockbox.hartshorn.util.introspect.scan.TypeCollectionException;
import org.dockbox.hartshorn.util.introspect.scan.TypeReference;
import org.dockbox.hartshorn.util.introspect.scan.TypeReferenceCollector;
import org.dockbox.hartshorn.util.introspect.scan.TypeReferenceCollectorContext;
import org.dockbox.hartshorn.util.introspect.view.TypeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collector for types in the environment. This delegates to the {@link TypeReferenceCollector type reference collectors}
 * defined in the {@link TypeReferenceCollectorContext} attached to the environment's {@link ApplicationContext}. If no
 * such context is available, a warning is logged and an empty collection is returned.
 *
 * <p>All type references are resolved to their respective classes and then introspected using the environment's
 * {@link org.dockbox.hartshorn.util.introspect.Introspector}. Note that while classes are loaded, it is not ensured they
 * are immediately initialized.
 *
 * @see TypeReferenceCollector
 * @see TypeReferenceCollectorContext
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public class EnvironmentTypeCollector {

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentTypeCollector.class);

    private final ApplicationEnvironment environment;
    private final TypeReferenceCollectorContext collectorContext;

    public EnvironmentTypeCollector(ApplicationEnvironment environment, TypeReferenceCollectorContext collectorContext) {
        this.environment = environment;
        this.collectorContext = collectorContext;
    }

    /**
     * Collects all types that match the given predicate. The collection is performed by delegating to the
     * {@link TypeReferenceCollector type reference collectors} defined in the {@link TypeReferenceCollectorContext}
     * attached to the environment's {@link ApplicationContext}.
     *
     * @param predicate the predicate to match
     * @param <T> the type of the elements in the collection
     *
     * @return a collection of types that match the given predicate
     */
    public <T> Collection<TypeView<? extends T>> types(Predicate<TypeView<?>> predicate) {
        try {
            Set<TypeReference> references = this.collectorContext.collector().collect();
            Collection<Class<?>> classes = this.loadClasses(references);
            return classes.stream()
                    .map(this.environment.introspector()::introspect)
                    .filter(predicate)
                    .map(reference -> (TypeView<T>) reference)
                    .collect(Collectors.toSet());
        }
        catch (TypeCollectionException e) {
            this.environment.handle(e);
            return Collections.emptyList();
        }
    }

    private Collection<Class<?>> loadClasses(Collection<TypeReference> references) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return references.stream()
                .map(reference -> {
                    try {
                        return reference.getOrLoad(classLoader);
                    }
                    catch (ClassReferenceLoadException e) {
                        this.environment.handle(e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
