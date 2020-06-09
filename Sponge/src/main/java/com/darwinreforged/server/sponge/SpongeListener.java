package com.darwinreforged.server.sponge;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.events.util.CancellableEvent;
import com.darwinreforged.server.core.events.internal.chat.SendChatMessageEvent;
import com.darwinreforged.server.core.events.internal.player.InventoryInteractionEvent;
import com.darwinreforged.server.core.events.internal.player.PlayerLoggedInEvent;
import com.darwinreforged.server.core.events.internal.player.PlayerMoveEvent;
import com.darwinreforged.server.core.events.internal.player.PlayerTeleportEvent;
import com.darwinreforged.server.core.events.internal.server.ServerReloadEvent;
import com.darwinreforged.server.core.player.PlayerManager;
import com.darwinreforged.server.core.types.location.DarwinLocation;
import com.darwinreforged.server.sponge.implementations.SpongeLocationUtils;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import xyz.olivermartin.multichat.spongebridge.MultiChatSponge;

public class SpongeListener {

    @Listener
    public void onServerReload(GameReloadEvent event) {
        DarwinServer.getEventBus().post(new ServerReloadEvent(null));
    }

    @Listener
    public void onPlayerTeleport(MoveEntityEvent.Teleport event, @First Player p) {
        DarwinLocation fromLocation = SpongeLocationUtils.getDarwinLocationFrom(event.getFromTransform().getLocation());
        DarwinLocation toLocation = SpongeLocationUtils.getDarwinLocationFrom(event.getToTransform().getLocation());
        postCancellable(new PlayerTeleportEvent(DarwinServer.getUtilMan().get(PlayerManager.class).getPlayer(p.getUniqueId(), p.getName()), fromLocation, toLocation), event);
    }

    @Listener
    public void onPlayerMove(MoveEntityEvent event, @First Player p) {
        postCancellable(new PlayerMoveEvent(DarwinServer.getUtilMan().get(PlayerManager.class).getPlayer(p.getUniqueId(), p.getName())), event);
    }

    @Listener
    public void onPlayerLoggedIn(ClientConnectionEvent.Join event, @First Player p) {
        DarwinServer.getEventBus().post(new PlayerLoggedInEvent(DarwinServer.getUtilMan().get(PlayerManager.class).getPlayer(p.getUniqueId(), p.getName())));
    }

    @Listener
    public void onInventoryInteract(InteractInventoryEvent event, @First Player p) {
        postCancellable(new InventoryInteractionEvent(DarwinServer.getUtilMan().get(PlayerManager.class).getPlayer(p.getUniqueId(), p.getName())), event);
    }

    @Listener
    public void onChatMessageSent(MessageChannelEvent.Chat event, @First Player p) {
        String channel = MultiChatSponge.playerChannels.getOrDefault(p, "global");
        postCancellable(new SendChatMessageEvent(DarwinServer.getUtilMan().get(PlayerManager.class).getPlayer(p.getUniqueId(), p.getName()), event.getRawMessage().toPlain(), channel.equalsIgnoreCase("global")), event);
    }

    private <I extends CancellableEvent> void postCancellable(I e, Cancellable se) {
        DarwinServer.getEventBus().post(e);
        if (e.isCancelled()) se.setCancelled(true);
    }
}
