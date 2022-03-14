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

package org.dockbox.hartshorn.web;

import org.dockbox.hartshorn.core.annotations.inject.Provider;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.services.parameter.ParameterLoader;
import org.dockbox.hartshorn.web.annotations.UseHttpServer;
import org.dockbox.hartshorn.web.processing.HttpServletParameterLoader;
import org.dockbox.hartshorn.web.servlet.ErrorServlet;
import org.dockbox.hartshorn.web.servlet.WebServlet;
import org.dockbox.hartshorn.web.servlet.WebServletImpl;

@Service(activators = UseHttpServer.class)
public abstract class HttpServerProviders {

    @Provider
    public ErrorServlet errorServlet() {
        return new ErrorServletImpl();
    }

    @Provider
    public Class<? extends WebServlet> webServlet() {
        return WebServletImpl.class;
    }

    @Provider
    public Class<WebServletImpl> webServletImpl() {
        return WebServletImpl.class;
    }

    @Provider
    public Class<ServletHandler> servletHandler() {
        return ServletHandler.class;
    }

    @Provider("http_webserver")
    public ParameterLoader parameterLoader() {
        return new HttpServletParameterLoader();
    }
}