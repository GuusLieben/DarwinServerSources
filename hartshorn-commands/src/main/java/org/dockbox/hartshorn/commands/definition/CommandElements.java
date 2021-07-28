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

package org.dockbox.hartshorn.commands.definition;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.i18n.permissions.Permission;
import org.dockbox.hartshorn.commands.CommandSource;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class containing common definitions for {@link CommandElement command elements}.
 */
public final class CommandElements {

    private CommandElements() {
    }

    /**
     * Creates a new {@link CommandElement} for the given enum <code>type</code>. The element's suggestions will
     * use the possible enum values.
     * @param name The name of the element
     * @param permission The permission required for the element
     * @param type The (enum) type of the element
     * @param optional Whether the element is optional
     * @param <E> The type parameter for the enum type
     * @return The enum command element
     */
    public static <E extends Enum<E>> CommandElement<E> enumElement(String name, Permission permission, Class<E> type, boolean optional) {
        return new EnumCommandElement<>(name, permission, type, optional);
    }

    private static class EnumCommandElement<E extends Enum<E>> implements CommandElement<E> {

        private final String name;
        private final Permission permission;
        private final Map<String, E> values;
        private final boolean optional;

        private EnumCommandElement(String name, Permission permission, Class<E> type, boolean optional) {
            this.name = name;
            this.permission = permission;
            this.values = Arrays.stream(type.getEnumConstants())
                    .collect(Collectors.toMap(value -> value.name().toLowerCase(),
                            Function.identity(), (value, value2) -> {
                                throw new UnsupportedOperationException(type.getCanonicalName() + " contains more than one enum constant "
                                        + "with the same name, only differing by capitalization, which is unsupported.");
                            }
                    ));
            this.optional = optional;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public Exceptional<Permission> permission() {
            return Exceptional.of(this.permission);
        }

        @Override
        public boolean optional() {
            return this.optional;
        }

        @Override
        public Exceptional<E> parse(CommandSource source, String argument) {
            return Exceptional.of(this.values.get(argument.toLowerCase()));
        }

        @Override
        public Collection<String> suggestions(CommandSource source, String argument) {
            return HartshornUtils.asUnmodifiableCollection(this.values.keySet()).stream()
                    .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(argument.toLowerCase(Locale.ROOT)))
                    .toList();
        }

        @Override
        public int size() {
            return 1;
        }
    }

}
