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

package org.dockbox.hartshorn.launchpad;

import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.launchpad.activation.ActivatorHolder;

/**
 * An {@link InjectionCapableApplication} of which the enabled functionality can be configured and expanded upon
 * using {@link ActivatorHolder}s.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public interface ConfigurableActivationInjectionCapableApplication extends InjectionCapableApplication {

    ActivatorHolder activators();
}
