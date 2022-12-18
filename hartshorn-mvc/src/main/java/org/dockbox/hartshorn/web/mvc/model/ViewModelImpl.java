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

package org.dockbox.hartshorn.web.mvc.model;

import org.dockbox.hartshorn.util.option.Option;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ViewModelImpl implements ViewModel {

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> attributes() {
        return this.attributes;
    }

    @Override
    public void attribute(final String name, final Object value) {
        this.attributes.put(name, value);
    }

    @Override
    public Option<Object> attribute(final String name) {
        return Option.of(this.attributes.get(name));
    }
}