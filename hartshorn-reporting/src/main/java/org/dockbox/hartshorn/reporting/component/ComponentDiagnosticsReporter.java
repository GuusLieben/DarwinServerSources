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

package org.dockbox.hartshorn.reporting.component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.dockbox.hartshorn.inject.annotations.Component;
import org.dockbox.hartshorn.inject.component.ApplicationMainComponentContainer;
import org.dockbox.hartshorn.inject.component.ComponentContainer;
import org.dockbox.hartshorn.inject.component.ComponentRegistry;
import org.dockbox.hartshorn.launchpad.ApplicationContext;
import org.dockbox.hartshorn.reporting.CategorizedDiagnosticsReporter;
import org.dockbox.hartshorn.reporting.ConfigurableDiagnosticsReporter;
import org.dockbox.hartshorn.reporting.DiagnosticsPropertyCollector;
import org.dockbox.hartshorn.reporting.Reportable;

/**
 * A {@link Reportable} that reports information about the components that are registered in the application context. This
 * reporter can be configured to group components by their {@link ComponentAttribute attribute}. Individual components are
 * reported using {@link ComponentContainerReporter}s.
 *
 * @see ComponentContainerReporter
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public class ComponentDiagnosticsReporter implements ConfigurableDiagnosticsReporter<ComponentReportingConfiguration>, CategorizedDiagnosticsReporter {

    public static final String COMPONENTS_CATEGORY = "components";

    private final ComponentReportingConfiguration configuration = new ComponentReportingConfiguration();
    private final ApplicationContext applicationContext;

    public ComponentDiagnosticsReporter(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void report(DiagnosticsPropertyCollector collector) {
        ComponentRegistry componentRegistry = this.applicationContext.get(ComponentRegistry.class);

        if (this.configuration.groupBy() == ComponentAttribute.NONE) {
            Reportable[] reporters = this.diagnosticsReporters(componentRegistry.containers());
            collector.property("all").writeDelegates(reporters);
        }
        else {
            Map<String, List<ComponentContainer<?>>> groupedContainers = componentRegistry.containers().stream()
                    .collect(Collectors.groupingBy(container -> switch (this.configuration.groupBy()) {
                        case STEREOTYPE -> stereotype(container).getCanonicalName();
                        case PACKAGE -> container.type().packageInfo().name();
                        default -> throw new IllegalStateException("Unexpected value: " + this.configuration.groupBy());
                    }));

            for (String key : groupedContainers.keySet()) {
                collector.property(key).writeDelegates(this.diagnosticsReporters(groupedContainers.get(key)));
            }
        }
    }

    @NonNull
    private Reportable[] diagnosticsReporters(Collection<ComponentContainer<?>> containers) {
        return containers.stream()
                .map(container -> (Reportable) new ComponentContainerReporter(this, this.applicationContext.environment(), container))
                .toArray(Reportable[]::new);
    }

    /**
     * Returns the stereotype of the given {@link ComponentContainer container}. The stereotype is the annotation that
     * is used to mark the component as a component, which is always a subtype of {@link Component}.
     *
     * @param container the container for which the stereotype should be returned
     * @return the stereotype of the given container
     */
    public static Class<?> stereotype(ComponentContainer<?> container) {
        if (container instanceof ApplicationMainComponentContainer<?> mainComponentContainer) {
            return mainComponentContainer.type().type();
        }
        else {
            Component component = container.type().annotations().get(Component.class).get();
            return component.annotationType();
        }
    }

    @Override
    public ComponentReportingConfiguration configuration() {
        return this.configuration;
    }

    @Override
    public String category() {
        return COMPONENTS_CATEGORY;
    }
}
