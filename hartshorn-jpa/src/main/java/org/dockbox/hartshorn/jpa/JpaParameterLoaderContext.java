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

package org.dockbox.hartshorn.jpa;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.util.ApplicationBoundParameterLoaderContext;
import org.dockbox.hartshorn.util.introspect.view.ExecutableElementView;

import jakarta.persistence.Query;

public class JpaParameterLoaderContext extends ApplicationBoundParameterLoaderContext {

    private final Query query;

    public JpaParameterLoaderContext(final ExecutableElementView<?> element, final Object instance,
                                     final ApplicationContext applicationContext, final Query query) {
        super(element, instance, applicationContext);
        this.query = query;
    }

    public Query query() {
        return this.query;
    }
}
