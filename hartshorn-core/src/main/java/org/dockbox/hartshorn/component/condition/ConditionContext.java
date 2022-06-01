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

package org.dockbox.hartshorn.component.condition;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.context.DefaultApplicationAwareContext;
import org.dockbox.hartshorn.util.reflect.AnnotatedElementContext;

public class ConditionContext extends DefaultApplicationAwareContext {

    private final AnnotatedElementContext<?> annotatedElementContext;
    private final RequiresCondition condition;

    public ConditionContext(final ApplicationContext applicationContext, final AnnotatedElementContext<?> annotatedElementContext, final RequiresCondition condition) {
        super(applicationContext);
        this.annotatedElementContext = annotatedElementContext;
        this.condition = condition;
    }

    public AnnotatedElementContext<?> annotatedElementContext() {
        return this.annotatedElementContext;
    }

    public RequiresCondition condition() {
        return this.condition;
    }
}