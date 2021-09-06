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

package org.dockbox.hartshorn.regions;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.regions.events.membership.MembershipChangedEvent;
import org.dockbox.hartshorn.regions.events.membership.MembershipRemovedEvent;
import org.dockbox.hartshorn.server.minecraft.players.Player;
import org.dockbox.hartshorn.server.minecraft.players.Players;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

public interface DefaultMembershipRegion extends MembershipRegion {

    @Override
    public default Collection<Player> players(RegionMembership membership) {
        Set<Player> players = HartshornUtils.emptySet();
        final Players service = Hartshorn.context().get(Players.class);
        for (Entry<UUID, RegionMembership> entry : this.memberships().entrySet()) {
            if (entry.getValue().equals(membership)) {
                final Exceptional<Player> player = service.player(entry.getKey());
                player.present(players::add);
            }
        }
        return HartshornUtils.asUnmodifiableSet(players);
    }

    @Override
    public default boolean hasMembership(Player player, RegionMembership membership) {
        final RegionMembership playerMembership = this.memberships().getOrDefault(player.uniqueId(), null);
        return playerMembership == membership;
    }

    @Override
    public default boolean hasAnyMembership(Player player, RegionMembership... memberships) {
        for (RegionMembership membership : memberships) {
            if (this.hasMembership(player, membership)) return true;
        }
        return false;
    }

    @Override
    public default void membership(Player player, RegionMembership membership) {
        this.memberships().put(player.uniqueId(), membership);
        new MembershipChangedEvent(this, player, membership).post();
    }

    @Override
    public default void remove(Player player) {
        this.memberships().remove(player.uniqueId());
        new MembershipRemovedEvent(this, player).post();
    }

    Map<UUID, RegionMembership> memberships();

}
