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

package org.dockbox.hartshorn.inject.processing;

import java.util.function.Supplier;
import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.inject.binding.HierarchicalBinder;
import org.dockbox.hartshorn.inject.scope.Scope;
import org.dockbox.hartshorn.util.collections.MultiMap;

/**
 * A {@link HierarchicalBinderPostProcessor} that delegates to a list of other {@link HierarchicalBinderPostProcessor}s. Each
 * processor is invoked in order according to its indicated {@link HierarchicalBinderPostProcessor#priority()}. However, the
 * composite processor itself has a priority of {@link ProcessingPriority#NORMAL_PRECEDENCE}, which could mean that ordering
 * may behave differently than expected if the composite is part of a list of processors itself.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class CompositeHierarchicalBinderPostProcessor implements HierarchicalBinderPostProcessor {

    private final Supplier<MultiMap<Integer, HierarchicalBinderPostProcessor>> postProcessors;

    public CompositeHierarchicalBinderPostProcessor(Supplier<MultiMap<Integer, HierarchicalBinderPostProcessor>> postProcessors) {
        this.postProcessors = postProcessors;
    }

    @Override
    public void process(InjectionCapableApplication application, Scope scope, HierarchicalBinder binder) {
        MultiMap<Integer, HierarchicalBinderPostProcessor> processors = this.postProcessors.get();
        for (Integer priority : processors.keySet()) {
            for(HierarchicalBinderPostProcessor processor : processors.get(priority)) {
                processor.process(application, scope, binder);
            }
        }
    }

    @Override
    public int priority() {
        return ProcessingPriority.NORMAL_PRECEDENCE;
    }
}
