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

package org.dockbox.hartshorn.commands.exceptions;

import org.dockbox.hartshorn.i18n.Message;

/**
 * The exception thrown when a command could not be parsed. This can occur either during
 * the creation of a command definition or {@link org.dockbox.hartshorn.commands.context.CommandContext}.
 */
public class ParsingException extends Exception {

    private final Message resource;

    public ParsingException(final Message resource) {
        super(resource.string());
        this.resource = resource;
    }

    public ParsingException(final Message resource, final Throwable cause) {
        super(resource.string(), cause);
        this.resource = resource;
    }
}
