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

package org.dockbox.hartshorn.launchpad.configuration;

import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.inject.binding.HierarchicalBinder;
import org.dockbox.hartshorn.inject.processing.HierarchicalBinderPostProcessor;
import org.dockbox.hartshorn.inject.scope.Scope;
import org.dockbox.hartshorn.inject.scope.ScopeKey;

import java.util.Set;
import java.util.function.Predicate;

/**
 * An adapter {@link HierarchicalBinderPostProcessor} that delegates to another {@link HierarchicalBinderPostProcessor} if
 * the {@link Scope} of the binder matches the provided {@link Predicate}. This is useful when adapting non-filtered
 * processors to be filtered by scope, and is effectively equal to extending {@link AbstractScopeFilteredBinderPostProcessor}
 * in the delegate processor.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class ScopeFilteredDelegateBinderPostProcessor extends AbstractScopeFilteredBinderPostProcessor {

    private final HierarchicalBinderPostProcessor processor;
    private final Predicate<ScopeKey> scopeFilter;

    public ScopeFilteredDelegateBinderPostProcessor(HierarchicalBinderPostProcessor processor, Predicate<ScopeKey> scopeFilter) {
        this.processor = processor;
        this.scopeFilter = scopeFilter;
    }

    public static ScopeFilteredDelegateBinderPostProcessor create(HierarchicalBinderPostProcessor processor, Predicate<ScopeKey> scopeFilter) {
        return new ScopeFilteredDelegateBinderPostProcessor(processor, scopeFilter);
    }

    public static ScopeFilteredDelegateBinderPostProcessor create(HierarchicalBinderPostProcessor processor, ScopeKey... permittedScopes) {
        return new ScopeFilteredDelegateBinderPostProcessor(processor, Set.of(permittedScopes)::contains);
    }

    @Override
    protected void processBinder(InjectionCapableApplication application, Scope scope, HierarchicalBinder binder) {
        this.processor.process(application, scope, binder);
    }

    @Override
    protected boolean supportsScope(InjectionCapableApplication application, Scope scope) {
        return scope != null && this.scopeFilter.test(scope.installableScopeType());
    }

    @Override
    public int priority() {
        return this.processor.priority();
    }
}
