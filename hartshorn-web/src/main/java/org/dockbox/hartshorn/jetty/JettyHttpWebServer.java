/*
 * Copyright (C) 2020 Guus Lieben
 *
 * This framework is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.hartshorn.jetty;

import org.dockbox.hartshorn.api.exceptions.ApplicationException;
import org.dockbox.hartshorn.di.annotations.inject.Binds;
import org.dockbox.hartshorn.di.context.ApplicationContext;
import org.dockbox.hartshorn.di.properties.Attribute;
import org.dockbox.hartshorn.di.properties.AttributeHolder;
import org.dockbox.hartshorn.di.properties.UseFactory;
import org.dockbox.hartshorn.jetty.error.JettyErrorAdapter;
import org.dockbox.hartshorn.persistence.properties.ModifiersAttribute;
import org.dockbox.hartshorn.web.DefaultHttpWebServer;
import org.dockbox.hartshorn.web.RequestHandlerContext;
import org.dockbox.hartshorn.web.HttpWebServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.inject.Inject;

@Binds(HttpWebServer.class)
public class JettyHttpWebServer extends DefaultHttpWebServer implements AttributeHolder {

    @Inject private ApplicationContext context;
    @Inject private final ServletHandler handler;
    private ModifiersAttribute mappingModifier = ModifiersAttribute.of();

    public JettyHttpWebServer() {
        super();
        this.handler = new ServletHandler();
    }

    @Override
    public void start(final int port) throws ApplicationException {
        try {
            final Server server = new Server(port);
            server.setHandler(this.handler);
            server.setErrorHandler(this.errorHandler());
            server.start();
        } catch (final Exception e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public void register(final RequestHandlerContext context) {
        this.handler.addServletWithMapping(this.createHolder(context), context.pathSpec());
    }

    @Override
    public void apply(final Attribute<?> property) {
        if (property instanceof ModifiersAttribute modifier) this.mappingModifier = modifier;
    }

    protected ErrorHandler errorHandler() {
        return this.context.get(JettyErrorAdapter.class);
    }

    protected ServletHolder createHolder(final RequestHandlerContext context) {
        return new ServletHolder(this.servlet(context));
    }

    protected JettyServletAdapter servlet(final RequestHandlerContext context) {
        return this.context.get(JettyServletAdapter.class, new UseFactory(this, context), this.mappingModifier);
    }

}