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

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.domain.tuple.Vector3N;
import org.dockbox.hartshorn.di.annotations.service.Service;
import org.dockbox.hartshorn.di.properties.AttributeHolder;
import org.dockbox.hartshorn.i18n.text.Text;
import org.dockbox.hartshorn.persistence.FileManager;
import org.dockbox.hartshorn.regions.flags.PersistentFlagModel;
import org.dockbox.hartshorn.regions.flags.RegionFlag;
import org.dockbox.hartshorn.regions.persistence.CustomRegion;
import org.dockbox.hartshorn.regions.persistence.RegionsList;
import org.dockbox.hartshorn.server.minecraft.Interactable;
import org.dockbox.hartshorn.server.minecraft.dimension.BlockSnapshot;
import org.dockbox.hartshorn.server.minecraft.dimension.position.Location;
import org.dockbox.hartshorn.server.minecraft.dimension.world.World;
import org.dockbox.hartshorn.server.minecraft.players.Player;
import org.dockbox.hartshorn.toolbinding.ItemTool;
import org.dockbox.hartshorn.toolbinding.ToolInteractionContext;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

@Service(id = "regions")
public class DefaultRegionService implements RegionService, AttributeHolder {

    @Inject
    private FileManager fileManager;
    private RegionsList regions;
    private Path regionStorageDir;

    private ItemTool tool;

    public Set<PersistentFlagModel> flags() {
        return this.regions.flags();
    }

    @Override
    public <R extends Region> Exceptional<R> first(final Location location, final Class<R> type) {
        return this.first(location.world(), location.vector(), type);
    }

    public void add(final RegionFlag<?> flag) {
        this.regions.save(flag);
    }

    @Override
    public <R extends Region> Exceptional<R> first(final Player player, final Class<R> type) {
        return this.first(player.location(), type);
    }

    public void add(final CustomRegion element) {
        this.regions.save(element);
    }

    @Override
    public <R extends Region> Exceptional<R> first(final World world, final int x, final int y, final Class<R> type) {
        return this.first(world, Vector3N.of(x, -1, y), type);
    }

    @Override
    public void enable() {
        this.regionStorageDir = this.fileManager.data(DefaultRegionService.class);
        this.fileManager.createPathIfNotExists(this.regionStorageDir);
        this.regions = RegionsList.restore(this.regionStorageDir);
        this.tool = ItemTool.builder()
                .name(Text.of("$1Region wand"))
                .perform(this::handle)
                .build();
    }

    @Override
    public boolean canEnable() {
        return this.regionStorageDir == null || this.fileManager == null || this.regions == null || this.tool == null;
    }

    private final Map<UUID, Vector3N> pos1 = HartshornUtils.emptyMap();
    private final Map<UUID, Vector3N> pos2 = HartshornUtils.emptyMap();

    private void handle(final ToolInteractionContext context) {
        // TODO GLieben, #342 this method is currently used for a raw implementation, this should be delegated to a handler type
        final Interactable target = context.target();
        if (target instanceof BlockSnapshot block) {
            final Location location = block.location();
            final int position = switch (context.type()) {
                case PRIMARY, EITHER -> 1;
                case SECONDARY -> 2;
            };
            final Player player = context.player();

            switch (position) {
                case 1: this.pos1.put(player.uniqueId(), location.vector());
                case 2: this.pos2.put(player.uniqueId(), location.vector());
            }

            player.send(Text.of("$1Region position $2%s $1set to $1%s".formatted(position, location)));

            final Vector3N pos1 = this.pos1.get(player.uniqueId());
            final Vector3N pos2 = this.pos2.get(player.uniqueId());

            if (pos1 == null || pos2 == null) return;

            final CustomRegion region = this.create(Text.of(player.name() + "'s region"), pos1, pos2, player, location.world());
            this.add(region);
            player.send(Text.of("Created region with ID " + region.id()));
        }
    }

    protected CustomRegion create(final Text name, final Vector3N cornerA, final Vector3N cornerB, final Player owner, final World world) {
        return new CustomRegion(name, cornerA, cornerB, owner.uniqueId(), world.worldUniqueId());
    }

    private <R extends Region> Exceptional<R> first(final World world, final Vector3N position, final Class<R> type) {
        if (CustomRegion.class.equals(type) || Region.class.equals(type)) {
            // TODO
        }
        return Exceptional.empty();
    }

    @Override
    public <R extends Region> Set<R> all(final Location location, final Class<R> type) {
        return this.all(location.world(), location.vector(), type);
    }

    @Override
    public <R extends Region> Set<R> all(final Player player, final Class<R> type) {
        return this.all(player.location(), type);
    }

    @Override
    public <R extends Region> Set<R> all(final World world, final int x, final int y, final Class<R> type) {
        return this.all(world, Vector3N.of(x, -1, y), type);
    }

    private <R extends Region> Set<R> all(final World world, final Vector3N position, final Class<R> type) {
        if (CustomRegion.class.equals(type) || Region.class.equals(type)) {
            // TODO
        }
        return HartshornUtils.emptySet();
    }

    @Override
    public void register(final RegionFlag<?> flag) {
        this.regions.save(flag);
    }

    @Override
    public Exceptional<RegionFlag<?>> flag(final String id) {
        for (final PersistentFlagModel flag : this.regions.flags()) {
            if (id.equals(flag.id())) return Exceptional.of(flag.restore());
        }
        return Exceptional.empty();
    }

    @Override
    public ItemTool tool() {
        return this.tool;
    }


}
