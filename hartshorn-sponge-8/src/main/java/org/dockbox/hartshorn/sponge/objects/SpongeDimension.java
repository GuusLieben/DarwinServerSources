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

package org.dockbox.hartshorn.sponge.objects;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.domain.tuple.Vector3N;
import org.dockbox.hartshorn.server.minecraft.dimension.BlockDimension;
import org.dockbox.hartshorn.server.minecraft.dimension.EntityHolding;
import org.dockbox.hartshorn.server.minecraft.dimension.position.BlockFace;
import org.dockbox.hartshorn.server.minecraft.entities.Entity;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.item.storage.MinecraftItems;
import org.dockbox.hartshorn.server.minecraft.players.Profile;
import org.dockbox.hartshorn.sponge.util.SpongeConversionUtil;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.World;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

public interface SpongeDimension extends BlockDimension, EntityHolding {

    @Override
    default Vector3N minimumPosition() {
        return SpongeConversionUtil.fromSponge(this.getExtent().blockMin().toDouble());
    }

    @Override
    default Vector3N maximumPosition() {
        return SpongeConversionUtil.fromSponge(this.getExtent().blockMax().toDouble());
    }

    @Override
    default Vector3N floor(Vector3N position) {
        Vector3i floor = this.getExtent().highestPositionAt(SpongeConversionUtil.toSponge(position).toInt());
        return SpongeConversionUtil.fromSponge(floor);
    }

    @Override
    default boolean hasBlock(Vector3N position) {
        Vector3i loc = SpongeConversionUtil.toSponge(position);
        return this.getExtent().containsBlock(loc);
    }

    @Override
    default Exceptional<org.dockbox.hartshorn.server.minecraft.item.Item> getBlock(Vector3N position) {
        Vector3i loc = SpongeConversionUtil.toSponge(position);
        BlockState blockState = this.getExtent().block(loc);
        if (blockState.type() == BlockTypes.AIR.get()) return Exceptional.of(MinecraftItems.getInstance().getAir());
        ItemStack stack = ItemStack.builder().fromBlockState(blockState).build();
        return Exceptional.of(SpongeConversionUtil.fromSponge(stack));
    }

    @Override
    default boolean setBlock(Vector3N position, Item item, BlockFace direction, Profile placer) {
        Vector3i loc = SpongeConversionUtil.toSponge(position);
        Optional<BlockType> blockType = SpongeConversionUtil.toSponge(item).type().block();
        if (blockType.isEmpty()) return false;
        BlockState state = blockType.get().defaultState();
        return this.getExtent().setBlock(loc, state);
    }

    @Override
    default Collection<Entity> getEntities() {
        return this.getEntities(e -> true);
    }

    @Override
    default Collection<Entity> getEntities(Predicate<Entity> predicate) {
        return this.getExtent().entities(this.aabb(), entity -> {
            Entity hartshornEntity = SpongeConversionUtil.fromSponge(entity);
            return predicate.test(hartshornEntity);
        }).stream().map(SpongeConversionUtil::fromSponge).toList();
    }

    private AABB aabb() {
        return AABB.of(
                SpongeConversionUtil.toSponge(this.minimumPosition()),
                SpongeConversionUtil.toSponge(this.maximumPosition())
        );
    }

    World<?, ?> getExtent();

}
