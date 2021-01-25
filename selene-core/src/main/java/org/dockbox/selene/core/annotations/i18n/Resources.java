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

package org.dockbox.selene.core.annotations.i18n;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface to indicate a type defines one or more {@link org.dockbox.selene.core.i18n.entry.Resource resource(s)}.
 * This is typically picked up by {@link org.dockbox.selene.core.i18n.common.ResourceService} to collect all known
 * {@link org.dockbox.selene.core.i18n.entry.Resource resources} during init phases.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Resources {
    /**
     * The module responsible for the resources. {@link org.dockbox.selene.core.i18n.entry.IntegratedResource Internal resources}
     * are linked to {@link org.dockbox.selene.core.server.Selene}.
     *
     * @return the responsible module class
     */
    Class<?> module();
}
