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

package org.dockbox.hartshorn.reporting.component;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.Component;
import org.dockbox.hartshorn.component.ComponentContainer;
import org.dockbox.hartshorn.component.ComponentLocator;
import org.dockbox.hartshorn.component.condition.RequiresCondition;
import org.dockbox.hartshorn.inject.Context;
import org.dockbox.hartshorn.inject.Required;
import org.dockbox.hartshorn.reporting.CategorizedDiagnosticsReporter;
import org.dockbox.hartshorn.reporting.ConfigurableDiagnosticsReporter;
import org.dockbox.hartshorn.reporting.DiagnosticsPropertyCollector;
import org.dockbox.hartshorn.reporting.Reportable;
import org.dockbox.hartshorn.reporting.component.ComponentReportingConfiguration.ComponentAttribute;
import org.dockbox.hartshorn.util.StringUtilities;
import org.dockbox.hartshorn.util.introspect.view.FieldView;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.inject.Named;

public class ComponentDiagnosticsReporter implements ConfigurableDiagnosticsReporter<ComponentReportingConfiguration>, CategorizedDiagnosticsReporter {

    public static final String COMPONENTS_CATEGORY = "components";

    private final ComponentReportingConfiguration configuration = new ComponentReportingConfiguration();
    private final ApplicationContext applicationContext;

    public ComponentDiagnosticsReporter(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void report(final DiagnosticsPropertyCollector collector) {
        final ComponentLocator componentLocator = this.applicationContext.get(ComponentLocator.class);

        if (this.configuration.groupBy() == ComponentAttribute.NONE) {
            final Reportable[] reporters = this.diagnosticsReporters(componentLocator.containers());
            collector.property("all").write(reporters);
        }
        else {
            final Map<String, List<ComponentContainer>> groupedContainers = componentLocator.containers().stream()
                    .collect(Collectors.groupingBy(container -> switch (this.configuration.groupBy()) {
                        case STEREOTYPE -> this.stereotype(container).getCanonicalName();
                        case PACKAGE -> container.type().packageInfo().name();
                        default -> throw new IllegalStateException("Unexpected value: " + this.configuration.groupBy());
                    }));

            for (final String key : groupedContainers.keySet()) {
                collector.property(key).write(this.diagnosticsReporters(groupedContainers.get(key)));
            }
        }
    }

    @NotNull
    private Reportable[] diagnosticsReporters(final Collection<ComponentContainer> containers) {
        return containers.stream()
                .map(container -> (Reportable) componentCollector -> {
                    componentCollector.property("type").write(container.type());
                    componentCollector.property("id").write(container.id());
                    componentCollector.property("name").write(container.name());
                    componentCollector.property("singleton").write(container.singleton());
                    componentCollector.property("lazy").write(container.lazy());

                    final String componentType = StringUtilities.capitalize(container.componentType().name().toLowerCase());
                    componentCollector.property("componentType").write(componentType);
                    componentCollector.property("permitsProxying").write(container.permitsProxying());
                    componentCollector.property("permitsProcessing").write(container.permitsProcessing());

                    if (this.configuration.includeDependencies()) {
                        componentCollector.property("dependencies").write(dependencyCollector -> {
                            dependencyCollector.property("inject").write(injectCollector -> reportAnnotatedField(container, Inject.class, injectCollector));
                            dependencyCollector.property("context").write(requiredCollector -> reportAnnotatedField(container, Context.class, requiredCollector));
                        });
                    }

                    if (this.configuration.includeRequiredConditions()) {
                        final Reportable[] reporters = container.type().annotations().all(RequiresCondition.class).stream()
                                .map(requiresCondition -> (Reportable) conditionCollector -> {
                                    conditionCollector.property("type").write(requiresCondition.annotationType().getCanonicalName());
                                    conditionCollector.property("condition").write(requiresCondition.condition().getCanonicalName());
                                    conditionCollector.property("failOnNoMatch").write(requiresCondition.failOnNoMatch());
                                }).toArray(Reportable[]::new);
                        componentCollector.property("conditions").write(reporters);
                    }

                    for (final Entry<ComponentAttribute, Boolean> entry : this.configuration.attributes().entrySet()) {
                        if (entry.getValue()) {
                            switch (entry.getKey()) {
                                case STEREOTYPE -> componentCollector.property("stereotype").write(this.stereotype(container).getCanonicalName());
                                case PACKAGE -> componentCollector.property("package").write(container.type().packageInfo());
                            }
                        }
                    }
                }).toArray(Reportable[]::new);
    }

    private static void reportAnnotatedField(final ComponentContainer container, final Class<? extends Annotation> annotation, final DiagnosticsPropertyCollector injectCollector) {
        final List<? extends FieldView<?, ?>> injectFields = container.type().fields().annotatedWith(annotation);
        for (final FieldView<?, ?> injectField : injectFields) {
            injectCollector.property(injectField.name()).write(fieldCollector -> {
                fieldCollector.property("type").write(injectField.type());
                fieldCollector.property("required").write(injectField.annotations().get(Required.class).map(Required::value).orElse(false));
                if (injectField.annotations().has(Named.class)) {
                    fieldCollector.property("name").write(injectField.annotations().get(Named.class).get().value());
                }
            });
        }
    }

    private Class<?> stereotype(final ComponentContainer container) {
        final Component component = container.type().annotations().get(Component.class).get();
        return component.annotationType();
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