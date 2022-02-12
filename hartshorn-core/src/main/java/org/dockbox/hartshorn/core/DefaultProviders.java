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

package org.dockbox.hartshorn.core;

import org.dockbox.hartshorn.core.annotations.activate.UseBootstrap;
import org.dockbox.hartshorn.core.annotations.activate.UseServiceProvision;
import org.dockbox.hartshorn.core.annotations.inject.Provider;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ConcreteContextCarrier;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.proxy.DelegatorAccessor;
import org.dockbox.hartshorn.core.proxy.DelegatorAccessorImpl;

import javax.inject.Singleton;

@Service(activators = { UseBootstrap.class, UseServiceProvision.class })
public class DefaultProviders {

    @Provider
    @Singleton
    public Class<? extends ContextCarrier> contextCarrier() {
        return ConcreteContextCarrier.class;
    }

    @Provider
    public Class<? extends DelegatorAccessor> delegatorAccessor() {
        return DelegatorAccessorImpl.class;
    }
}
