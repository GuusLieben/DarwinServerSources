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

package org.dockbox.selene.sponge;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;

import org.dockbox.selene.core.DiscordUtils;
import org.dockbox.selene.core.MinecraftVersion;
import org.dockbox.selene.core.PlayerStorageService;
import org.dockbox.selene.core.events.EventBus;
import org.dockbox.selene.core.events.packet.PacketEvent;
import org.dockbox.selene.core.objects.Exceptional;
import org.dockbox.selene.core.objects.Packet;
import org.dockbox.selene.core.server.Selene;
import org.dockbox.selene.core.server.ServerType;
import org.dockbox.selene.core.server.bootstrap.SeleneBootstrap;
import org.dockbox.selene.core.util.Reflect;
import org.dockbox.selene.core.util.SeleneUtils;
import org.dockbox.selene.nms.packets.NMSPacket;
import org.dockbox.selene.nms.properties.NativePacketProperty;
import org.dockbox.selene.sponge.listeners.SpongeCommandListener;
import org.dockbox.selene.sponge.listeners.SpongeDiscordListener;
import org.dockbox.selene.sponge.listeners.SpongePlayerListener;
import org.dockbox.selene.sponge.listeners.SpongeServerListener;
import org.dockbox.selene.sponge.objects.composite.Composite;
import org.dockbox.selene.sponge.objects.composite.ImmutableCompositeData;
import org.dockbox.selene.sponge.objects.composite.MutableCompositeData;
import org.dockbox.selene.sponge.objects.composite.CompositeDataManipulatorBuilder;
import org.dockbox.selene.sponge.util.SpongeInjector;
import org.dockbox.selene.sponge.util.SpongeTaskRunner;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Platform.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import eu.crushedpixel.sponge.packetgate.api.listener.PacketListener.ListenerPriority;
import eu.crushedpixel.sponge.packetgate.api.listener.PacketListenerAdapter;
import eu.crushedpixel.sponge.packetgate.api.registry.PacketConnection;
import eu.crushedpixel.sponge.packetgate.api.registry.PacketGate;

/**
 * Sponge API 7.x implementation of Selene, using events to initiate startup tasks.
 */
@Plugin(
    id = "selene",
    name = "Selene Server",
    description = "Custom plugins and modifications combined into a single source",
    url = "https://github.com/GuusLieben/Selene",
    authors = "GuusLieben",
    dependencies = {
        @Dependency(id = "plotsquared"),
        @Dependency(id = "nucleus"),
        @Dependency(id = "luckperms")
    }
)
public class SpongeAPI7Bootstrap extends SeleneBootstrap {

    @Inject
    private PluginContainer container;

    private final SpongeDiscordListener discordListener = new SpongeDiscordListener();

    /**
     * Creates a new Selene instance using the {@link org.dockbox.selene.sponge.util.SpongeInjector} bindings
     * providing utilities.
     */
    public SpongeAPI7Bootstrap() {
        super(new SpongeInjector());
    }

    /**
     * The entry point of application, in case it is started directly.
     *
     * @param args
     *     The input arguments
     */
    public static void main(String[] args) {
        // This is the only place where SystemOut is allowed as no server instance can exist at this point.
        //noinspection UseOfSystemOutOrSystemErr
        System.out.println("Selene is a framework plugin, it should not be started as a separate application.");

        // This will cause Forge to complain about direct System.exit references. This only results in a warning
        // message and an automatic redirect to FMLCommonHandler.exitJava.
        System.exit(8);
    }

    @Listener
    public void onGamePreInit(GamePreInitializationEvent event) {
        Composite.ITEM_KEY = Key.builder()
            .type(new TypeToken<MapValue<String, Object>>() {
            })
            .query(DataQuery.of(Composite.QUERY))
            .id(Composite.ID)
            .name(Composite.NAME)
            .build();

        DataRegistration.builder()
            .dataClass(MutableCompositeData.class)
            .immutableClass(ImmutableCompositeData.class)
            .builder(new CompositeDataManipulatorBuilder())
            .id(Composite.ID)
            .name(Composite.NAME)
            .build();
    }

    private void registerSpongeListeners(Object... listeners) {
        for (Object obj : listeners) {
            if (null != obj)
                Sponge.getEventManager().registerListeners(this, obj);
            else
                Selene.log().warn("Attempted to register 'null' listener");
        }
    }

    /**
     * Sponge Listener method, registers additional listeners present in
     * {@link org.dockbox.selene.sponge.listeners.SpongeServerListener}.
     *
     * @param event
     *     Sponge's initialization event
     */
    @Listener
    public void onServerInit(GameInitializationEvent event) {
        this.registerSpongeListeners(
            Selene.provide(SpongeCommandListener.class),
            Selene.provide(SpongeServerListener.class),
            Selene.provide(SpongeDiscordListener.class),
            Selene.provide(SpongePlayerListener.class)
        );

        super.init();

        Optional<PacketGate> packetGate = Sponge.getServiceManager().provide(PacketGate.class);
        if (packetGate.isPresent()) {
            this.preparePacketGateListeners(packetGate.get());
            Selene.log().info("Successfully hooked into PacketGate");
        } else {
            Selene.log().warn("Missing PacketGate, packet events will not be fired!");
        }
    }

    private void preparePacketGateListeners(PacketGate packetGate) {
        EventBus bus = Selene.provide(EventBus.class);
        Set<Class<? extends Packet>> adaptedPackets = SeleneUtils.emptySet();
        bus.getListenersToInvokers().forEach((k, v) -> {
            v.forEach(eventWrapper -> {
                if (Reflect.isAssignableFrom(PacketEvent.class, eventWrapper.getEventType())) {
                    Class<? extends Packet> packet = eventWrapper.getMethod()
                        .getAnnotation(org.dockbox.selene.core.annotations.event.filter.Packet.class).value();

                    // Adapters post the event globally, so we only need to register it once. This also avoids double-posting of the same event.
                    if (!adaptedPackets.contains(packet)) {
                        Packet emptyPacket = Selene.provide(packet);
                        packetGate.registerListener(
                            this.getPacketGateAdapter(packet),
                            ListenerPriority.DEFAULT,
                            emptyPacket.getNativePacketType()
                        );
                        adaptedPackets.add(packet);
                    }
                }
            });
        });
    }

    private PacketListenerAdapter getPacketGateAdapter(Class<? extends Packet> packet) {
        return new PacketListenerAdapter() {
            @Override
            public void onPacketWrite(eu.crushedpixel.sponge.packetgate.api.event.PacketEvent packetEvent, PacketConnection connection) {
                Selene.provide(PlayerStorageService.class).getPlayer(connection.getPlayerUUID()).ifPresent(player -> {
                    // Shadowed NMS type
                    net.minecraft.network.Packet<?> nativePacket = packetEvent.getPacket();
                    Packet internalPacket = Selene.provide(packet, new NativePacketProperty<>(nativePacket));

                    PacketEvent<? extends Packet> event = new PacketEvent<>(internalPacket, player).post();
                    packetEvent.setCancelled(event.isCancelled());
                    if (event.isModified() && internalPacket instanceof NMSPacket) packetEvent.setPacket(((NMSPacket<?>) internalPacket).getPacket());
                });
            }
        };
    }

    @NotNull
    @Override
    public ServerType getServerType() {
        return ServerType.SPONGE;
    }

    @Override
    public String getPlatformVersion() {
        return Sponge.getPlatform().getContainer(Component.IMPLEMENTATION).getVersion().orElse("Unknown");
    }

    @Override
    public MinecraftVersion getMinecraftVersion() {
        return MinecraftVersion.MC1_12;
    }

    /**
     * Sponge Listener method, registers the MagiBridge JDA instance as soon as it is available.
     *
     * Sometimes MagiBridge takes a while to start, if this is the case we register a delayed task to execute
     * this method again 30 seconds later.
     *
     * @param event
     *     The event
     */
    @Listener
    public void onServerStartedLate(GameStartedServerEvent event) {
        Exceptional<JDA> oj = Selene.provide(DiscordUtils.class).getJDA();
        if (oj.isPresent()) {
            JDA jda = oj.get();
            // Avoid registering it twice if the scheduler outside this condition is executing this twice.
            // Usually cancelling all tasks would be preferred, however any module is able to schedule tasks
            // we may not want to cancel.
            if (!jda.getRegisteredListeners().contains(this.discordListener)) {
                jda.addEventListener(this.discordListener);
                Selene.log().info("Initiated JDA" + JDAInfo.VERSION);
            }
        } else {
            // Attempt to get the JDA once every 10 seconds until successful
            new SpongeTaskRunner().acceptDelayed(() -> this.onServerStartedLate(event), 10, TimeUnit.SECONDS);
        }
    }

    public static PluginContainer getContainer() {
        return ((SpongeAPI7Bootstrap) getInstance()).container;
    }

}
