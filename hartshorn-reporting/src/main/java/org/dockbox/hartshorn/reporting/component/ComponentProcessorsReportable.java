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

package org.dockbox.hartshorn.reporting.component;

import org.dockbox.hartshorn.component.processing.ComponentPostProcessor;
import org.dockbox.hartshorn.reporting.DiagnosticsPropertyCollector;
import org.dockbox.hartshorn.reporting.Reportable;
import org.dockbox.hartshorn.util.collections.MultiMap;

import java.util.Collection;

public class ComponentProcessorsReportable implements Reportable {
    private final MultiMap<Integer, ComponentPostProcessor> processors;

    public ComponentProcessorsReportable(final MultiMap<Integer, ComponentPostProcessor> processors) {
        this.processors = processors;
    }

    @Override
    public void report(final DiagnosticsPropertyCollector propertyCollector) {
        final Reportable[] reportables = processors.values().stream()
                .flatMap(Collection::stream)
                .map(processor -> (Reportable) processorCollector -> {
                    processorCollector.property("name").write(processor.getClass().getCanonicalName());
                    processorCollector.property("order").write(processor.priority());
                }).toArray(Reportable[]::new);

        propertyCollector.property("processors").write(reportables);
    }
}