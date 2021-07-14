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

package org.dockbox.hartshorn.server.minecraft;

/**
 * Enum type providing the supported Minecraft versions for Hartshorn. Typically this is used when
 * defining vanilla constants, indicating in which version they were made available.
 */
public enum MinecraftVersion {
    MC1_16("1.16"),
    INDEV("Indev");

    private final String readableVersionString;

    MinecraftVersion(String readableVersionString) {
        this.readableVersionString = readableVersionString;
    }

    public String asString() {
        return this.readableVersionString;
    }
}
