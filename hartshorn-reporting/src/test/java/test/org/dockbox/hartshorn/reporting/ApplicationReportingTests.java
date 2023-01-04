/*
 * Copyright 2019-2023 the original author or authors.
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

package test.org.dockbox.hartshorn.reporting;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.beans.UseBeanScanning;
import org.dockbox.hartshorn.component.factory.UseFactoryServices;
import org.dockbox.hartshorn.proxy.UseProxying;
import org.dockbox.hartshorn.reporting.DiagnosticsReport;
import org.dockbox.hartshorn.reporting.DiagnosticsReportCollector;
import org.dockbox.hartshorn.util.GroupNode;
import org.dockbox.hartshorn.util.Node;
import org.dockbox.hartshorn.reporting.Reportable;
import org.dockbox.hartshorn.reporting.application.ApplicationDiagnosticsReporter;
import org.dockbox.hartshorn.reporting.component.ComponentDiagnosticsReporter;
import org.dockbox.hartshorn.reporting.component.ComponentProcessorDiagnosticsReporter;
import org.dockbox.hartshorn.reporting.system.SystemDiagnosticsReporter;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

@UseBeanScanning
@UseProxying
@UseFactoryServices
@HartshornTest
public class ApplicationReportingTests {

    @Inject
    private ApplicationContext applicationContext;

    @Test
    void testApplicationDiagnosticsReportingPasses() {
        final Reportable configurable = this.applicationContext.get(Reportable.class);
        final DiagnosticsReportCollector collector = this.applicationContext.get(DiagnosticsReportCollector.class);

        final DiagnosticsReport report = collector.report(configurable);
        Assertions.assertNotNull(report);
    }

    @Test
    void testApplicationDiagnosticsIncludeRequiredDiagnostics() {
        final Reportable configurable = this.applicationContext.get(Reportable.class);
        final DiagnosticsReportCollector collector = this.applicationContext.get(DiagnosticsReportCollector.class);

        final DiagnosticsReport report = collector.report(configurable);
        final Node<?> root = report.root();

        Assertions.assertNotNull(report);
        Assertions.assertNotNull(root);
        Assertions.assertTrue(root instanceof GroupNode);

        final GroupNode group = (GroupNode) root;
        Assertions.assertFalse(group.value().isEmpty());

        Assertions.assertTrue(group.has(ApplicationDiagnosticsReporter.APPLICATION_CATEGORY));
        Assertions.assertTrue(group.has(ComponentDiagnosticsReporter.COMPONENTS_CATEGORY));
        Assertions.assertTrue(group.has(ComponentProcessorDiagnosticsReporter.COMPONENT_PROCESSORS_CATEGORY));
        Assertions.assertTrue(group.has(SystemDiagnosticsReporter.SYSTEM_CATEGORY));
    }
}
