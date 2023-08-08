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

package org.dockbox.hartshorn.inject;

import org.dockbox.hartshorn.application.context.ApplicationContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CompositeDependencyResolver implements DependencyResolver {

    private final Set<DependencyResolver> resolvers;

    public CompositeDependencyResolver(final Set<DependencyResolver> resolvers) {
        this.resolvers = resolvers;
    }

    @Override
    public Set<DependencyContext<?>> resolve(final Collection<DependencyDeclarationContext<?>> containers, final ApplicationContext applicationContext) throws DependencyResolutionException {
        final Set<DependencyContext<?>> dependencyContexts = new HashSet<>();
        for (final DependencyResolver resolver : this.resolvers) {
            final Set<DependencyContext<?>> resolvedDependencies = resolver.resolve(containers, applicationContext);
            dependencyContexts.addAll(resolvedDependencies);
        }
        return dependencyContexts;
    }
}