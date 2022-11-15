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

package org.dockbox.hartshorn.jpa.remote;

import org.dockbox.hartshorn.config.annotations.ConfigurationObject;
import org.dockbox.hartshorn.util.option.Option;

import java.util.Map;

@ConfigurationObject(prefix = "hartshorn.data")
public class DataSourceConfigurations {

    private Map<String, DataSourceConfiguration> sources;

    public DataSourceConfigurations() {
    }

    public Map<String, DataSourceConfiguration> sources() {
        return this.sources;
    }

    public Option<DataSourceConfiguration> source(final String id) {
        return Option.of(this.sources.get(id));
    }
}