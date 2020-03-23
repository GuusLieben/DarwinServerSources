package com.darwinreforged.servermodifications.plugins;

import com.darwinreforged.servermodifications.resources.Translations;
import com.darwinreforged.servermodifications.util.PlayerUtils;
import com.google.inject.Inject;
import eu.crushedpixel.sponge.packetgate.api.event.PacketEvent;
import eu.crushedpixel.sponge.packetgate.api.listener.PacketListenerAdapter;
import eu.crushedpixel.sponge.packetgate.api.registry.PacketConnection;
import eu.crushedpixel.sponge.packetgate.api.registry.PacketGate;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Plugin(id = "personaltime", name = "Personal Time", description = "Allows players to set their own personal time of day", version = "1.0-PRERELEASE-1", dependencies = @Dependency(id = "packetgate"))
public class PlayerTimePlugin extends PacketListenerAdapter {

    @Inject
    private Logger logger;

    private Map<UUID, Long> timeOffsets; // <Player UUID, time offset in ticks>

    public PlayerTimePlugin() {
    }

    @Listener
    public void onInitializationEvent(GameInitializationEvent event) {
        timeOffsets = new HashMap<>();
        Optional<PacketGate> packetGateOptional = Sponge.getServiceManager().provide(PacketGate.class);
        if (packetGateOptional.isPresent()) {
            PacketGate packetGate = packetGateOptional.get();
            packetGate.registerListener(this, ListenerPriority.DEFAULT, SPacketTimeUpdate.class);
            initializeCommands();
            logger.info("Personal Time has successfully initialized");
        } else {
            logger.error("PacketGate is not installed. Personal Time depends on PacketGate in order to work");
        }
    }

    private void initializeCommands() {
        CommandSpec personalTimeSetCommand = CommandSpec.builder()
                .permission("personaltime.command.set")
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("time"))))
                .executor((src, args) -> {
                    if (!(src instanceof Player)) {
                        PlayerUtils.tell(src, Translations.PLAYER_ONLY_COMMAND.t());
                        return CommandResult.empty();
                    }
                    Player player = (Player) src;
                    timeOffsets.putIfAbsent(player.getUniqueId(), 0L);
                    Optional<String> optionalTime = args.getOne("time");
                    if (optionalTime.isPresent()) {
                        String time = optionalTime.get();
                        if (time.equalsIgnoreCase("day")) {
                            setPersonalTime(player, 1000);
                            return CommandResult.success();
                        } else if (time.equalsIgnoreCase("night")) {
                            setPersonalTime(player, 14000);
                            return CommandResult.success();
                        } else {
                            int intTime;
                            try {
                                intTime = Integer.parseInt(time);
                            } catch (NumberFormatException e) {
                                PlayerUtils.tell(player, Translations.PTIME_INVALID_NUMBER.ft(time));
                                return CommandResult.empty();
                            }
                            if (intTime < 0) {
                                PlayerUtils.tell(player, Translations.PTIME_NUMBER_TOO_SMALL.ft(time));
                                return CommandResult.empty();
                            }
                            setPersonalTime(player, intTime);
                            return CommandResult.success();
                        }
                    }
                    return CommandResult.empty();
                })
                .build();

        CommandSpec personalTimeResetCommand = CommandSpec.builder()
                .permission("personaltime.command.reset")
                .executor((src, args) -> {
                    if (!(src instanceof Player)) {
                        PlayerUtils.tell(src, Translations.PLAYER_ONLY_COMMAND.t());
                        return CommandResult.empty();
                    }
                    Player player = (Player) src;
                    resetPersonalTime(player);
                    return CommandResult.success();
                })
                .build();

        CommandSpec personalTimeStatusCommand = CommandSpec.builder()
                .permission("personaltime.command.status")
                .executor((src, args) -> {
                    if (!(src instanceof Player)) {
                        PlayerUtils.tell(src, Translations.PLAYER_ONLY_COMMAND.t());
                        return CommandResult.empty();
                    }
                    Player player = (Player) src;
                    timeOffsets.putIfAbsent(player.getUniqueId(), 0L);
                    long ticksAhead = timeOffsets.get(player.getUniqueId());
                    if (ticksAhead == 0) {
                        PlayerUtils.tell(player, Translations.PTIME_IN_SYNC.t());
                    } else {
                        PlayerUtils.tell(player, Translations.PTIME_AHEAD.ft(ticksAhead));
                    }
                    return CommandResult.success();
                })
                .build();

        CommandSpec personalTimeCommand = CommandSpec.builder()
                .permission("personaltime.command")
                .child(personalTimeSetCommand, "set")
                .child(personalTimeResetCommand, "reset")
                .child(personalTimeStatusCommand, "status")
                .build();

        Sponge.getCommandManager().register(this, personalTimeCommand, "personaltime", "ptime");
    }

    private String ticksToRealTime(long ticks) {
        int hours = (int) (ticks / 1000.0) + 6;
        int minutes = (int) (((ticks % 1000) / 1000.0) * 60.0);

        String suffix = "AM";

        if (hours >= 12) {
            hours -= 12;
            suffix = "PM";
            if (hours >= 12) {
                hours -= 12;
                suffix = "AM";
            }
        }

        if (hours == 0) {
            hours += 12;
        }

        return hours + ":" + String.format("%02d", minutes) + " " + suffix;
    }

    private void setPersonalTime(Player player, long ticks) {
        World world = player.getWorld();
        long worldTime = world.getProperties().getWorldTime();
        long desiredPersonalTime = (long) Math.ceil(worldTime / 24000.0f) * 24000 + ticks; // Fast forward to the next '0' time and add the desired number of ticks
        long timeOffset = desiredPersonalTime - worldTime;
        timeOffsets.put(player.getUniqueId(), timeOffset);
    }

    private void resetPersonalTime(Player player) {
        timeOffsets.put(player.getUniqueId(), 0L);
    }

    @Override
    public void onPacketWrite(PacketEvent packetEvent, PacketConnection connection) {
        if (!(packetEvent.getPacket() instanceof SPacketTimeUpdate)) {
            return;
        }

        UUID playerUuid = connection.getPlayerUUID();
        timeOffsets.putIfAbsent(playerUuid, 0L);

        SPacketTimeUpdate packet = (SPacketTimeUpdate) packetEvent.getPacket();
        PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer(16));
        try {
            packet.writePacketData(packetBuffer);
        } catch (IOException e) {
            logger.error("Failed to read packet buffer");
            return;
        }

        long totalWorldTime = packetBuffer.readLong();
        long worldTime = packetBuffer.readLong();

        long personalWorldTime;
        if (worldTime < 0) {
            personalWorldTime = worldTime - timeOffsets.get(playerUuid); // gamerule doDaylightCycle is false, which makes worldTime negative
        } else {
            personalWorldTime = worldTime + timeOffsets.get(playerUuid);
        }

        packetEvent.setPacket(new SPacketTimeUpdate(totalWorldTime, personalWorldTime, true));
    }
}
