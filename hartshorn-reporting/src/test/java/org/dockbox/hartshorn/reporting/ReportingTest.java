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

package org.dockbox.hartshorn.reporting;

import org.dockbox.hartshorn.application.HartshornApplication;
import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.beans.UseBeanScanning;
import org.dockbox.hartshorn.component.factory.UseFactoryServices;
import org.dockbox.hartshorn.proxy.UseProxying;
import org.dockbox.hartshorn.reporting.aggregate.AggregateDiagnosticsReporter;
import org.dockbox.hartshorn.reporting.application.ApplicationDiagnosticsReporter;
import org.dockbox.hartshorn.reporting.collect.DiagnosticsReport;
import org.dockbox.hartshorn.reporting.collect.StandardDiagnosticsReportCollector;
import org.dockbox.hartshorn.reporting.system.SystemDiagnosticsReporter;
import org.junit.jupiter.api.Test;

@UseBeanScanning
@UseProxying
@UseFactoryServices
public class ReportingTest {

    @Test
    void testSystemReporter() {
        final ApplicationContext applicationContext = HartshornApplication.create(ReportingTest.class);
        final AggregateDiagnosticsReporter reporter = new AggregateDiagnosticsReporter();
        reporter.configuration().add(new SystemDiagnosticsReporter());
        reporter.configuration().add(new ApplicationDiagnosticsReporter(applicationContext));

        final StandardDiagnosticsReportCollector collector = new StandardDiagnosticsReportCollector();
        reporter.report(collector);

        final DiagnosticsReport report = collector.report();
        String json = report.serialize(new JsonReportSerializer());
        System.out.println(json);
        // TODO: Validate contents of report
    }
}