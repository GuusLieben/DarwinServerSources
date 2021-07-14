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

package org.dockbox.hartshorn.sponge.game;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.domain.tuple.Tristate;
import org.dockbox.hartshorn.api.domain.tuple.Vector3N;
import org.dockbox.hartshorn.api.exceptions.Except;
import org.dockbox.hartshorn.api.exceptions.NotImplementedException;
import org.dockbox.hartshorn.api.i18n.common.Language;
import org.dockbox.hartshorn.api.i18n.common.ResourceEntry;
import org.dockbox.hartshorn.api.i18n.entry.DefaultResources;
import org.dockbox.hartshorn.api.i18n.permissions.Permission;
import org.dockbox.hartshorn.api.i18n.text.Text;
import org.dockbox.hartshorn.api.i18n.text.pagination.Pagination;
import org.dockbox.hartshorn.di.annotations.inject.Wired;
import org.dockbox.hartshorn.server.minecraft.dimension.Block;
import org.dockbox.hartshorn.server.minecraft.dimension.position.Location;
import org.dockbox.hartshorn.server.minecraft.entities.Entity;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.packets.Packet;
import org.dockbox.hartshorn.server.minecraft.players.GameSettings;
import org.dockbox.hartshorn.server.minecraft.players.Gamemode;
import org.dockbox.hartshorn.server.minecraft.players.Hand;
import org.dockbox.hartshorn.server.minecraft.players.Player;
import org.dockbox.hartshorn.server.minecraft.players.Profile;
import org.dockbox.hartshorn.server.minecraft.players.SimpleGameSettings;
import org.dockbox.hartshorn.server.minecraft.players.Sounds;
import org.dockbox.hartshorn.server.minecraft.players.inventory.PlayerInventory;
import org.dockbox.hartshorn.sponge.game.entity.SpongeEntity;
import org.dockbox.hartshorn.sponge.inventory.SpongePlayerInventory;
import org.dockbox.hartshorn.sponge.util.SpongeConvert;
import org.dockbox.hartshorn.sponge.util.SpongeUtil;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.data.DataHolder.Mutable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.util.blockray.RayTraceResult;
import org.spongepowered.api.world.Locatable;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class SpongePlayer extends Player implements SpongeEntity<net.minecraft.server.level.ServerPlayer, org.spongepowered.api.entity.living.player.Player>, SpongeComposite {

    private static final int RAY_TRACE_LIMIT = 50;

    @Wired
    public SpongePlayer(@NotNull UUID uniqueId, @NotNull String name) {
        super(uniqueId, name);
    }

    @Override
    public void execute(String command) {
        this.player().present(player -> {
            try {
                Sponge.server().commandManager().process(player, command);
            }
            catch (CommandException e) {
                Except.handle(e);
            }
        });
    }

    @Override
    public void send(ResourceEntry text) {
        this.send(text.translate(this).asText());
    }

    @Override
    public void send(Text text) {
        this.player().present(player -> player.sendMessage(SpongeConvert.toSponge(text)));
    }

    @Override
    public void sendWithPrefix(ResourceEntry text) {
        this.sendWithPrefix(text.translate(this).asText());
    }

    @Override
    public void sendWithPrefix(Text text) {
        this.player().present(player -> {
            final Text message = Text.of(DefaultResources.instance().prefix(), text);
            player.sendMessage(SpongeConvert.toSponge(message));
        });
    }

    @Override
    public void send(Pagination pagination) {
        this.player().present(player -> SpongeConvert.toSponge(pagination).sendTo(player));
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.hasPermission(permission, SubjectData.GLOBAL_CONTEXT);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        if (permission.context().absent()) {
            return this.hasPermission(permission.get());
        }
        else {
            Set<Context> contexts = SpongeConvert.toSponge(permission.context().get());
            return this.hasPermission(permission.get(), contexts);
        }
    }

    private boolean hasPermission(String permission, Set<Context> contexts) {
        return this.user().map(user -> user.hasPermission(permission, contexts)).or(false);
    }

    @Override
    public void permission(String permission, Tristate state) {
        this.permission(permission, SubjectData.GLOBAL_CONTEXT, state);
    }

    @Override
    public void permission(Permission permission, Tristate state) {
        if (permission.context().absent()) {
            this.permission(permission.get(), state);
        } else {
            Set<Context> contexts = SpongeConvert.toSponge(permission.context().get());
            this.permission(permission.get(), contexts, state);
        }
    }

    public void permission(String permission, Set<Context> context, Tristate state) {
        org.spongepowered.api.util.Tristate tristate = SpongeConvert.toSponge(state);
        this.user().present(user -> user.subjectData().setPermission(context, permission, tristate));
    }

    @Override
    public EntityType<org.spongepowered.api.entity.living.player.Player> type() {
        return EntityTypes.PLAYER.get();
    }

    @Override
    public void send(Packet packet) {
        // TODO: Implement once packet API is done
        throw new NotImplementedException();
    }

    @Override
    public boolean online() {
        return this.player().map(ServerPlayer::isOnline).or(false);
    }

    @Override
    public void kick(Text reason) {
        this.player().present(player -> player.kick(SpongeConvert.toSponge(reason)));
    }

    @Override
    public Gamemode gamemode() {
        return SpongeUtil.get(this.player(), Keys.GAME_MODE, SpongeConvert::fromSponge, () -> Gamemode.OTHER);
    }

    @Override
    public SpongePlayer gamemode(Gamemode gamemode) {
        this.player().present(player -> {
            final GameMode mode = SpongeConvert.toSponge(gamemode);
            player.offer(Keys.GAME_MODE, mode);
        });
        return this;
    }

    @Override
    public Item itemInHand(Hand hand) {
        return this.inventory().slot(hand.slot());
    }

    @Override
    public void itemInHand(Hand hand, Item item) {
        this.inventory().slot(item, hand.slot());
    }

    @Override
    public void play(Sounds sound) {
        this.player().present(player -> SpongeConvert.toSponge(sound).present(soundType -> {
            final Sound playableSound = Sound.sound(soundType, Source.MASTER, 1, 1);
            player.playSound(playableSound);
        }));
    }

    @Override
    public boolean sneaking() {
        return this.bool(Keys.IS_SNEAKING);
    }

    @Override
    public Profile profile() {
        return new SpongeProfile(this.uniqueId());
    }

    @Override
    public Exceptional<Block> lookingAtBlock() {
        return this.player()
                .map(player -> this.trace(RayTrace.block(), player))
                .map(RayTraceResult::hitPosition)
                .map(SpongeConvert::fromSponge)
                .map(vector -> Location.of(vector, this.world()))
                .map(Block::from);
    }

    @Override
    public Exceptional<Entity> lookingAtEntity() {
        return this.player()
                .map(player -> this.trace(RayTrace.entity(), player))
                .map(RayTraceResult::selectedObject)
                .map(SpongeConvert::fromSponge);
    }

    private <T extends Locatable> RayTraceResult<T> trace(RayTrace<T> trace, ServerPlayer player) {
        return trace
                .sourceEyePosition(player)
                .continueWhileBlock(block -> {
                    final BlockType type = block.blockState().type();
                    return type == BlockTypes.AIR.get() ||
                            type == BlockTypes.CAVE_AIR.get() ||
                            type == BlockTypes.VOID_AIR.get();
                })
                .limit(RAY_TRACE_LIMIT)
                .direction(player)
                .world(player.world())
                .execute().orElse(null);
    }

    @Override
    public PlayerInventory inventory() {
        return new SpongePlayerInventory(this);
    }

    @Override
    public GameSettings gameSettings() {
        return this.player().map(player -> {
            final Locale locale = player.locale();
            final Language language = Language.of(locale);
            return new SimpleGameSettings(language);
        }).orElse(() -> new SimpleGameSettings(Language.EN_US)).get();
    }

    @Override
    public Vector3N rotation() {
        return this.player().map(player -> SpongeConvert.fromSponge(player.headRotation().get()))
                .or(Vector3N.empty());
    }

    private Exceptional<User> user() {
        return Exceptional.of(Sponge.server().userManager().find(this.uniqueId()));
    }

    public Exceptional<ServerPlayer> player() {
        return this.user().map(user -> user.player().orElse(null));
    }

    @Override
    public Exceptional<? extends Mutable> dataHolder() {
        // Use offline user reference to ensure we can (almost) always obtain the information
        return this.user();
    }

    @Override
    public Exceptional<org.spongepowered.api.entity.living.player.Player> spongeEntity() {
        return this.player().map(org.spongepowered.api.entity.living.player.Player.class::cast);
    }
}
