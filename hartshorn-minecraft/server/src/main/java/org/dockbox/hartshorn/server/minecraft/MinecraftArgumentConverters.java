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

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.domain.tuple.Vector3N;
import org.dockbox.hartshorn.commands.arguments.CommandValueConverter;
import org.dockbox.hartshorn.commands.arguments.DefaultArgumentConverters;
import org.dockbox.hartshorn.commands.definition.ArgumentConverter;
import org.dockbox.hartshorn.di.annotations.service.Service;
import org.dockbox.hartshorn.di.properties.InjectableType;
import org.dockbox.hartshorn.di.properties.InjectorProperty;
import org.dockbox.hartshorn.server.minecraft.dimension.Block;
import org.dockbox.hartshorn.server.minecraft.dimension.Worlds;
import org.dockbox.hartshorn.server.minecraft.dimension.position.Location;
import org.dockbox.hartshorn.server.minecraft.dimension.world.World;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.item.ItemContext;
import org.dockbox.hartshorn.server.minecraft.players.Player;
import org.dockbox.hartshorn.server.minecraft.players.Players;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.util.UUID;

@Service
public final class MinecraftArgumentConverters implements InjectableType {

    public static final ArgumentConverter<World> WORLD = CommandValueConverter.builder(World.class, "world")
            .withConverter(in -> {
                Worlds wss = Hartshorn.context().get(Worlds.class);
                Exceptional<World> world = wss.world(in);
                return world.orElse(
                        () -> {
                            UUID uuid = UUID.fromString(in);
                            return wss.world(uuid).orNull();
                        });
            }).build();

    public static final ArgumentConverter<Location> LOCATION = CommandValueConverter.builder(Location.class, "location", "position", "pos")
            .withConverter((cs, in) -> {
                String[] xyzw = in.split(",");
                String xyz = String.join(",", xyzw[0], xyzw[1], xyzw[2]);
                Vector3N vec = DefaultArgumentConverters.VECTOR.convert(cs, xyz).or(Vector3N.of(0, 0, 0));
                World world = WORLD.convert(cs, xyzw[3]).or(World.empty());

                return Exceptional.of(Location.of(vec, world));
            }).build();

    public static final ArgumentConverter<Player> PLAYER = CommandValueConverter.builder(Player.class, "player", "user")
            .withConverter(in -> {
                Players pss = Hartshorn.context().get(Players.class);
                Exceptional<Player> player = pss.player(in);
                return player.orElse(() -> {
                    try {
                        UUID uuid = UUID.fromString(in);
                        return pss.player(uuid).orNull();
                    }
                    catch (IllegalArgumentException e) {
                        //noinspection ReturnOfNull
                        return null;
                    }
                });
            }).withSuggestionProvider(in -> Hartshorn.context().get(Players.class).onlinePlayers().stream()
                    .map(Player::name)
                    .filter(n -> n.startsWith(in))
                    .toList())
            .build();

    public static final ArgumentConverter<Item> ITEM = CommandValueConverter.builder(Item.class, "item")
            .withConverter(in -> Exceptional.of(Item.of(in)))
            .withSuggestionProvider(in -> Hartshorn.context()
                    .first(ItemContext.class)
                    .map(ItemContext::items)
                    .orElse(HartshornUtils::emptyList).get())
            .build();

    public static final ArgumentConverter<Block> BLOCK = CommandValueConverter.builder(Block.class, "block")
            .withConverter(in -> Exceptional.of(Block.of(in)))
            .withSuggestionProvider(in -> Hartshorn.context()
                    .first(ItemContext.class)
                    .map(ItemContext::blocks)
                    .orElse(HartshornUtils::emptyList).get())
            .build();

    @Override
    public void enable(InjectorProperty<?>... properties) {
        Hartshorn.log().info("Registered Minecraft specific command argument converters.");
    }
}
