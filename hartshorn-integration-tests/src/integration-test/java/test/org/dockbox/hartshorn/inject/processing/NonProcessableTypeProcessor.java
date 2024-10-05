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

package test.org.dockbox.hartshorn.inject.processing;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.inject.processing.ComponentPostProcessor;
import org.dockbox.hartshorn.inject.processing.ComponentProcessingContext;
import org.dockbox.hartshorn.inject.processing.ProcessingPriority;
import org.junit.jupiter.api.Assertions;

public class NonProcessableTypeProcessor extends ComponentPostProcessor {

    @Override
    public <T> void postConfigureComponent(InjectionCapableApplication application, @Nullable T instance, ComponentProcessingContext<T> processingContext) {
        if (instance instanceof NonProcessableType) {
            try {
                processingContext.type().fields().named("nonNullIfProcessed").get().set(instance, "processed");
            }
            catch(Throwable throwable) {
                Assertions.fail(throwable);
            }
        }
    }

    @Override
    public int priority() {
        return ProcessingPriority.NORMAL_PRECEDENCE;
    }
}
