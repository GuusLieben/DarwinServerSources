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

import java.util.Set;

import org.dockbox.hartshorn.inject.ComponentKey;
import org.dockbox.hartshorn.inject.ComponentKeyResolver;
import org.dockbox.hartshorn.inject.component.ComponentContainer;
import org.dockbox.hartshorn.inject.condition.RequiresCondition;
import org.dockbox.hartshorn.inject.targets.ComponentInjectionPoint;
import org.dockbox.hartshorn.inject.targets.ComponentInjectionPointsResolver;
import org.dockbox.hartshorn.launchpad.environment.ApplicationEnvironment;
import org.dockbox.hartshorn.reporting.DiagnosticsPropertyCollector;
import org.dockbox.hartshorn.reporting.Reportable;
import org.dockbox.hartshorn.reporting.support.AnnotationReporter;

/**
 * A reportable that reports the contents of a {@link ComponentContainer} instance. This includes the following information:
 * <ul>
 *     <li>Component class type, {@link ComponentContainer#type()}</li>
 *     <li>Component ID, {@link ComponentContainer#id()}</li>
 *     <li>Component name, {@link ComponentContainer#name()}</li>
 *     <li>Component lifecycle type, {@link ComponentContainer#lifecycle()}</li>
 *     <li>Component lazy status, {@link ComponentContainer#lazy()}</li>
 *     <li>Whether the component permits proxying, {@link ComponentContainer#permitsProxying()}</li>
 *     <li>Whether the component permits processing, {@link ComponentContainer#permitsProcessing()}</li>
 *     <li>Component stereotype, {@link ComponentDiagnosticsReporter#stereotype(ComponentContainer)}</li>
 *     <li>Conditions that are required for the component to be active</li>
 *     <li>Dependencies of the component. Dependencies are resolved according to the rules defined by the active environment.</li>
 * </ul>
 *
 * <p>Each of these can be enabled or disabled individually, using the {@link ComponentReportingConfiguration} that
 * is provided by this reporter.
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
class ComponentContainerReporter implements Reportable {

    private final ComponentDiagnosticsReporter componentDiagnosticsReporter;
    private final ApplicationEnvironment environment;
    private final ComponentContainer<?> container;

    public ComponentContainerReporter(ComponentDiagnosticsReporter componentDiagnosticsReporter,
                                      ApplicationEnvironment environment,
                                      ComponentContainer<?> container) {
        this.componentDiagnosticsReporter = componentDiagnosticsReporter;
        this.environment = environment;
        this.container = container;
    }

    @Override
    public void report(DiagnosticsPropertyCollector componentCollector) {
        componentCollector.property("type").writeDelegate(this.container.type());
        componentCollector.property("id").writeString(this.container.id());
        componentCollector.property("name").writeString(this.container.name());
        componentCollector.property("lifecycle").writeEnum(this.container.lifecycle());
        componentCollector.property("lazy").writeBoolean(this.container.lazy());
        componentCollector.property("permitsProxying").writeBoolean(this.container.permitsProxying());
        componentCollector.property("permitsProcessing").writeBoolean(this.container.permitsProcessing());

        if (this.componentDiagnosticsReporter.configuration().includeDependencies()) {
            componentCollector.property("dependencies").writeDelegate((DiagnosticsPropertyCollector dependencyCollector) -> {
                dependencyCollector.property("inject").writeDelegate(this::reportAnnotatedField);
            });
        }

        if (this.componentDiagnosticsReporter.configuration().includeRequiredConditions()) {
            Reportable[] reporters = this.container.type().annotations().all(RequiresCondition.class)
                    .stream()
                    .map(requiresCondition -> (Reportable) conditionCollector -> {
                        conditionCollector.property("definition").writeDelegate(new AnnotationReporter<>(requiresCondition));
                        conditionCollector.property("condition").writeString(requiresCondition.condition().getCanonicalName());
                        conditionCollector.property("failOnNoMatch").writeBoolean(requiresCondition.failOnNoMatch());
                    }).toArray(Reportable[]::new);
            componentCollector.property("conditions").writeDelegates(reporters);
        }

        componentCollector.property("stereotype").writeString(ComponentDiagnosticsReporter.stereotype(this.container).getCanonicalName());
        componentCollector.property("package").writeDelegate(this.container.type().packageInfo());
    }

    private void reportAnnotatedField(DiagnosticsPropertyCollector injectCollector) {
        ComponentInjectionPointsResolver injectionPointsResolver = this.environment.injectionPointsResolver();
        ComponentKeyResolver componentKeyResolver = this.environment.componentKeyResolver();
        Set<? extends ComponentInjectionPoint<?>> injectionPoints = injectionPointsResolver.resolve(this.container.type());

        for (ComponentInjectionPoint<?> injectionPoint : injectionPoints) {
            injectCollector.property(injectionPoint.declaration().name()).writeDelegate(fieldCollector -> {
                ComponentKey<?> key = componentKeyResolver.resolve(injectionPoint.declaration());
                fieldCollector.property("key").writeDelegate(key);
            });
        }
    }
}
