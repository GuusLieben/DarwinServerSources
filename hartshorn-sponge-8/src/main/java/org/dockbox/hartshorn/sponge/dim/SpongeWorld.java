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

package org.dockbox.hartshorn.sponge.dim;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.domain.tuple.Vector3N;
import org.dockbox.hartshorn.server.minecraft.dimension.Chunk;
import org.dockbox.hartshorn.server.minecraft.dimension.position.BlockFace;
import org.dockbox.hartshorn.server.minecraft.dimension.position.Location;
import org.dockbox.hartshorn.server.minecraft.dimension.world.World;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.players.Profile;
import org.dockbox.hartshorn.sponge.util.SpongeConvert;
import org.dockbox.hartshorn.sponge.util.SpongeUtil;
import org.dockbox.hartshorn.util.HartshornUtils;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.gamerule.GameRule;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import lombok.Getter;

public class SpongeWorld extends World implements SpongeDimension {

    @Getter
    private final ResourceKey key;

    public SpongeWorld(ResourceKey key) {
        this.key = key;
    }

    @Override
    public boolean hasBlock(Vector3N position) {
        return this.world()
                .map(world -> world.containsBlock(SpongeConvert.toSponge(position)))
                .or(false);
    }

    @Override
    public Exceptional<Item> getBlock(Vector3N position) {
        // TODO: Implement when Block API (#303) is done
        return Exceptional.empty();
    }

    @Override
    public boolean setBlock(Vector3N position, Item item, BlockFace direction, Profile placer) {
        return this.world().map(world -> {
            // TODO: Implement when Block API (#303) is done
            // world.setBlock(SpongeConversionUtil.toSponge(position), null);
            return false;
        }).or(false);
    }

    @Override
    public Exceptional<Chunk> getChunk(Location location) {
        return this.getChunk(location.getVectorLoc());
    }

    @Override
    public Exceptional<Chunk> getChunk(Vector3N position) {
        final Vector3i vector3i = SpongeConvert.toSponge(position);
        final boolean hasChunk = this.world().map(world -> world.hasChunk(vector3i)).or(false);
        if (hasChunk) Exceptional.of(new SpongeChunk(this.key, vector3i));
        return Exceptional.empty();
    }

    @Override
    public Collection<Chunk> getLoadedChunks() {
        Collection<Chunk> chunks = HartshornUtils.emptyList();
        this.world().present(world -> {
            for (org.spongepowered.api.world.chunk.Chunk chunk : world.loadedChunks()) {
                chunks.add(new SpongeChunk(this.key, chunk.chunkPosition()));
            }
        });
        return HartshornUtils.asUnmodifiableCollection(chunks);
    }

    @Override
    public ServerWorld serverWorld() {
        return this.world().orNull();
    }

    @Override
    public int getPlayerCount() {
        return this.world().map(ServerWorld::players).map(Collection::size).or(0);
    }

    @Override
    public boolean unload() {
        return this.run(WorldManager::unloadWorld).or(false);
    }

    @Override
    public boolean load() {
        return this.run(WorldManager::loadWorld)
                .map(org.spongepowered.api.world.World::isLoaded)
                .or(false);
    }

    @Override
    public boolean isLoaded() {
        return this.world()
                .map(org.spongepowered.api.world.World::isLoaded)
                .or(false);
    }

    @Override
    public void setGamerule(String key, String value) {
        this.world().present(world -> {
            //noinspection unchecked
            final GameRule<String> rule = (GameRule<String>) SpongeUtil.spReference(RegistryTypes.GAME_RULE, value);
            world.properties().setGameRule(rule, value);
        });
    }

    @Override
    public Map<String, String> getGamerules() {
        Map<String, String> rules = HartshornUtils.emptyMap();
        this.world().present(world -> world.properties().gameRules().forEach((rule, value) -> rules.put(rule.name(), String.valueOf(value))));
        return null;
    }

    private <T> Exceptional<T> run(BiFunction<WorldManager, ResourceKey, CompletableFuture<T>> function) {
        final CompletableFuture<T> future = function.apply(Sponge.server().worldManager(), this.key);
        return SpongeUtil.await(future);
    }

    private Exceptional<ServerWorld> world() {
        return Exceptional.of(Sponge.server().worldManager().world(this.key));
    }
}