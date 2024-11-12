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

package org.dockbox.hartshorn.inject;

import org.dockbox.hartshorn.inject.targets.ComponentInjectionPoint;
import org.dockbox.hartshorn.inject.targets.ComponentInjectionPointsResolver;
import org.dockbox.hartshorn.proxy.ProxyOrchestrator;
import org.dockbox.hartshorn.util.introspect.Introspector;

/**
 * Represents the environment in which the {@link InjectionCapableApplication} is operating. This environment provides access
 * to the various components that are used by the application to perform its operations.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public interface InjectorEnvironment {

    /**
     * Gets the {@link ComponentKeyResolver} for the current environment. The resolver is responsible for resolving
     * {@link ComponentKey}s for a given element. This is typically used for resolving {@link ComponentKey}s for binding
     * declarations and injection points.
     *
     * @return The component key resolver
     */
    ComponentKeyResolver componentKeyResolver();

    /**
     * Gets the {@link ComponentInjectionPointsResolver} for the current environment. The resolver is responsible for
     * resolving {@link ComponentInjectionPoint}s for a given type.
     *
     * @return The component injection points resolver
     */
    ComponentInjectionPointsResolver injectionPointsResolver();

    /**
     * Gets the primary {@link Introspector} for this {@link InjectorEnvironment}. The introspector is responsible
     * for all introspection operations within the environment. This may or may not be the same as the binding for
     * {@link Introspector}, but is typically the same.
     *
     * @return The primary {@link Introspector}
     */
    Introspector introspector();

    /**
     * Gets the {@link ProxyOrchestrator} for the current environment. The orchestrator is responsible for all proxy
     * operations within the environment. Proxies may be created outside of the orchestrator, and depending on the
     * supported {@link org.dockbox.hartshorn.util.introspect.ProxyLookup proxy lookups} the orchestrator may or may not
     * support these external proxies.
     *
     * @return The proxy orchestrator
     */
    ProxyOrchestrator proxyOrchestrator();

    InjectorConfiguration configuration();

    ExceptionHandler exceptionHandler();
}
