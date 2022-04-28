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

package org.dockbox.hartshorn.data;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.Component;
import org.dockbox.hartshorn.util.Exceptional;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

@Component
public class ContextPropertyValueLookup implements ValueLookup {

    @Inject
    private ApplicationContext applicationContext;

    @Override
    public Exceptional<?> getValue(final String key) {
        return this.applicationContext.property(key);
    }

    @Override
    public Collection<?> getValues(final String key) {
        return this.applicationContext.properties(key).orElse(ArrayList::new).get();
    }
}
