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

package org.dockbox.hartshorn.data.remote;

import org.dockbox.hartshorn.component.Component;
import org.dockbox.hartshorn.data.annotations.ConfigurationObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConfigurationObject(prefix = "hartshorn.data")
public class StandardDataSourceList implements DataSourceList {

    private Map<String, DataSourceConfiguration> sources = new ConcurrentHashMap<>();

    @Override
    public void add(final String id, final DataSourceConfiguration configuration) {
        this.sources.put(id, configuration);
    }

    @Override
    public Map<String, DataSourceConfiguration> sources() {
        return this.sources;
    }

    @Override
    public DataSourceConfiguration get(final String id) {
        return this.sources.get(id);
    }

    @Override
    public DataSourceConfiguration defaultConnection() {
        return this.get("default");
    }
}