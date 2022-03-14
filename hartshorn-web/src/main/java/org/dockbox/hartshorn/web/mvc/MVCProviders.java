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

package org.dockbox.hartshorn.web.mvc;

import org.dockbox.hartshorn.core.annotations.inject.Provider;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.services.parameter.ParameterLoader;
import org.dockbox.hartshorn.web.annotations.UseMvcServer;
import org.dockbox.hartshorn.web.processing.MvcParameterLoader;
import org.dockbox.hartshorn.web.servlet.MvcServlet;

@Service(activators = UseMvcServer.class)
public class MVCProviders {

    @Provider("mvc_webserver")
    public ParameterLoader mvcParameterLoader() {
        return new MvcParameterLoader();
    }

    @Provider
    public Class<MvcServlet> mvcServlet() {
        return MvcServlet.class;
    }
}