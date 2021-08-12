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

package org.dockbox.hartshorn.server.minecraft.service;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.di.annotations.inject.Binds;
import org.dockbox.hartshorn.i18n.permissions.Permission;
import org.dockbox.hartshorn.i18n.text.Text;
import org.dockbox.hartshorn.server.minecraft.players.Player;
import org.dockbox.hartshorn.server.minecraft.players.Players;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@Binds(BroadcastService.class)
public class BroadcastServiceImpl implements BroadcastService {
    @Override
    public void broadcastPublic(@NotNull final Text message) {
        Hartshorn.context().get(Players.class).onlinePlayers().forEach(message::send);
    }

    @Override
    public void broadcastWithFilter(@NotNull final Text message, @NotNull final Predicate<Player> filter) {
        BroadcastServiceImpl.sendWithPredicate(message, filter);
    }

    @Override
    public void broadcastForPermission(@NotNull final Text message, @NotNull final Permission permission) {
        BroadcastServiceImpl.sendWithPredicate(message, p -> p.hasPermission(permission));
    }

    @Override
    public void broadcastForPermission(@NotNull final Text message, @NotNull final String permission) {
        BroadcastServiceImpl.sendWithPredicate(message, p -> p.hasPermission(permission));
    }

    @Override
    public void broadcastForPermissionWithFilter(@NotNull final Text message, @NotNull final Permission permission, @NotNull final Predicate<Player> filter) {
        BroadcastServiceImpl.sendWithPredicate(message, p -> p.hasPermission(permission) && filter.test(p));
    }

    @Override
    public void broadcastForPermissionWithFilter(@NotNull final Text message, @NotNull final String permission, @NotNull final Predicate<Player> filter) {
        BroadcastServiceImpl.sendWithPredicate(message, p -> p.hasPermission(permission) && filter.test(p));
    }

    private static void sendWithPredicate(final Text message, final Predicate<Player> filter) {
        Hartshorn.context().get(Players.class).onlinePlayers().stream()
                .filter(filter)
                .forEach(message::send);
    }
}