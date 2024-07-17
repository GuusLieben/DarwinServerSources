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

import java.util.Properties;

import org.dockbox.hartshorn.properties.PropertyRegistry;
import org.dockbox.hartshorn.reporting.DiagnosticsPropertyCollector;
import org.dockbox.hartshorn.reporting.Reportable;

/**
 * A reportable that reports the contents of a {@link Properties} instance. The keys of the properties are used as
 * property names, and the values of the properties are used as property values.
 *
 * @see Properties
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public class PropertiesReporter implements Reportable {

    private final PropertyRegistry registry;

    public PropertiesReporter(PropertyRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void report(DiagnosticsPropertyCollector collector) {
        for(String key : this.registry.keys()) {
            String value = this.registry.value(key)
                    .orElseGet(() -> "<empty>");
            collector.property(key).writeString(value);
        }
    }
}
