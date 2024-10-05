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

package org.dockbox.hartshorn.reporting.application;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.dockbox.hartshorn.context.ContextView;
import org.dockbox.hartshorn.context.NamedContext;
import org.dockbox.hartshorn.launchpad.ApplicationContext;
import org.dockbox.hartshorn.launchpad.Hartshorn;
import org.dockbox.hartshorn.launchpad.banner.HartshornLogoBanner;
import org.dockbox.hartshorn.launchpad.environment.ApplicationEnvironment;
import org.dockbox.hartshorn.launchpad.lifecycle.ObservableApplicationEnvironment;
import org.dockbox.hartshorn.launchpad.lifecycle.Observer;
import org.dockbox.hartshorn.reporting.CategorizedDiagnosticsReporter;
import org.dockbox.hartshorn.reporting.ConfigurableDiagnosticsReporter;
import org.dockbox.hartshorn.reporting.DiagnosticsPropertyCollector;
import org.dockbox.hartshorn.reporting.Reportable;

/**
 * A diagnostics reporter that reports information about the application. This includes the following information:
 * <ul>
 *     <li>Version of Hartshorn</li>
 *     <li>Location of the application JAR file</li>
 *     <li>Application properties</li>
 *     <li>Service activators</li>
 *     <li>Observers</li>
 *     <li>Application-level contexts</li>
 * </ul>
 *
 * <p>Each of these can be enabled or disabled individually, using the {@link ApplicationReportingConfiguration} that
 * is provided by this reporter.
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public class ApplicationDiagnosticsReporter implements ConfigurableDiagnosticsReporter<ApplicationReportingConfiguration>, CategorizedDiagnosticsReporter {

    /**
     * The category of this reporter.
     */
    public static final String APPLICATION_CATEGORY = "application";

    private final ApplicationReportingConfiguration configuration = new ApplicationReportingConfiguration();
    private final ApplicationContext applicationContext;

    public ApplicationDiagnosticsReporter(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void report(DiagnosticsPropertyCollector collector) {
        if (this.configuration.includeVersion()) {
            String implementationVersion = ApplicationContext.class.getPackage().getImplementationVersion();
            collector.property("version").writeString(implementationVersion != null ? implementationVersion : "unknown");
        }
        if (this.configuration.includeJarLocation()) {
            reportJarLocation(collector);
        }
        if (this.configuration.includeApplicationProperties()) {
            this.reportApplicationProperties(collector);
        }
        if (this.configuration.includeServiceActivators()) {
            this.reportServiceActivators(collector);
        }
        if (this.configuration.includeObservers()) {
            this.reportObservers(collector);
        }
        if (this.configuration.includeContexts()) {
            this.reportContexts(collector);
        }
    }

    /**
     * Reports the location of the application JAR file.
     *
     * @param collector the collector to write to
     */
    protected static void reportJarLocation(DiagnosticsPropertyCollector collector) {
        String jarLocation;
        try {
            jarLocation = Hartshorn.class.getProtectionDomain().getCodeSource().getLocation().toURI().toString();
        } catch (Exception e) {
            jarLocation = "Unknown";
        }
        collector.property("jar").writeString(jarLocation);
    }

    /**
     * Reports the application properties. This includes all properties that are available in the application context.
     *
     * @param collector the collector to write to
     */
    protected void reportApplicationProperties(DiagnosticsPropertyCollector collector) {
        Properties properties = this.applicationContext.properties().properties();
        Reportable reporter = new PropertiesReporter(properties);
        collector.property("properties").writeDelegate(reporter);
    }

    /**
     * Reports the canonical names of all service activators that are registered with the application context.
     *
     * @param collector the collector to write to
     */
    protected void reportServiceActivators(DiagnosticsPropertyCollector collector) {
        String[] activators = this.applicationContext.activators().activators().stream()
                .map(activator -> activator.annotationType().getCanonicalName())
                .toArray(String[]::new);
        collector.property("activators").writeStrings(activators);
    }

    /**
     * Reports the number of observers that are registered with the application context. Observers are reported by
     * their class name, and the number of instances that are registered. If the application environment is not an
     * instance of {@link ObservableApplicationEnvironment}, no observers are reported.
     *
     * @param collector the collector to write to
     */
    protected void reportObservers(DiagnosticsPropertyCollector collector) {
        ApplicationEnvironment environment = this.applicationContext.environment();
        if (environment instanceof ObservableApplicationEnvironment observable) {
            Map<Class<? extends Observer>, List<Observer>> observers = observable.observers(Observer.class).stream()
                            .collect(Collectors.groupingBy(Observer::getClass));

            collector.property("observers").writeDelegate(observerCollector -> {
                for (Entry<Class<? extends Observer>, List<Observer>> entry : observers.entrySet()) {
                    observerCollector.property(entry.getKey().getSimpleName()).writeInts(entry.getValue().size());
                }
            });
        }
    }

    /**
     * Reports all application-level contexts that are registered with the application context. Contexts are reported
     * by their class name. If a context is an instance of {@link Reportable}, its data is also reported. If a context
     * is an instance of {@link NamedContext}, its name is also reported. If a context has child contexts, these are
     * reported recursively.
     *
     * @param collector the collector to write to
     */
    protected void reportContexts(DiagnosticsPropertyCollector collector) {
        AtomicReference<BiConsumer<DiagnosticsPropertyCollector, ContextView>> reporterReference = new AtomicReference<>();

        BiConsumer<DiagnosticsPropertyCollector, ContextView> reporter = (contextsCollector, context) -> {
            contextsCollector.property("type").writeString(context.getClass().getCanonicalName());
            if (context instanceof Reportable reportable) {
                contextsCollector.property("data").writeDelegate(reportable);
            }
            if (context instanceof NamedContext namedContext) {
                contextsCollector.property("name").writeString(namedContext.name());
            }
            if (!context.contexts().isEmpty()) {
                Reportable[] childReporters = childReporters(reporterReference, context);
                contextsCollector.property("children").writeDelegates(childReporters);
            }
        };
        reporterReference.set(reporter);

        Reportable[] reporters = childReporters(reporterReference, this.applicationContext);
        collector.property("contexts").writeDelegates(reporters);
    }

    @NonNull
    private static Reportable[] childReporters(AtomicReference<BiConsumer<DiagnosticsPropertyCollector, ContextView>> reporterReference, ContextView context) {
        return context.contexts().stream()
                .map(childContext -> (Reportable) contextsController -> reporterReference.get().accept(contextsController, childContext))
                .toArray(Reportable[]::new);
    }

    @Override
    public ApplicationReportingConfiguration configuration() {
        return this.configuration;
    }

    @Override
    public String category() {
        return APPLICATION_CATEGORY;
    }
}
