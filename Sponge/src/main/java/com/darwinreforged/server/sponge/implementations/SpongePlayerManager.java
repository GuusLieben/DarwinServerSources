package com.darwinreforged.server.sponge.implementations;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.chat.Pagination;
import com.darwinreforged.server.core.math.Vector3d;
import com.darwinreforged.server.core.player.DarwinPlayer;
import com.darwinreforged.server.core.player.PlayerManager;
import com.darwinreforged.server.core.player.inventory.DarwinItem;
import com.darwinreforged.server.core.player.state.GameModes;
import com.darwinreforged.server.core.resources.Permissions;
import com.darwinreforged.server.core.resources.translations.DefaultTranslations;
import com.darwinreforged.server.core.types.living.Console;
import com.darwinreforged.server.core.types.living.MessageReceiver;
import com.darwinreforged.server.core.types.living.Target;
import com.darwinreforged.server.core.types.location.DarwinLocation;
import com.darwinreforged.server.core.types.location.DarwinWorld;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.darwinreforged.server.sponge.implementations.SpongeLocationUtils.getSpongeLocationFrom;

public class SpongePlayerManager extends PlayerManager {

    private static Text prefix() {
        return SpongeCommonUtils.fromAPI(com.darwinreforged.server.core.chat.Text.of(DefaultTranslations.PREFIX.s()));
    }

    @Override
    public void broadcast(com.darwinreforged.server.core.chat.Text message) {
        Sponge.getServer().getBroadcastChannel().send(Text.of(prefix(), SpongeCommonUtils.fromAPI(message)));
    }

    @Override
    public void broadcastIfPermitted(com.darwinreforged.server.core.chat.Text message, String permission) {
        Sponge.getServer().getOnlinePlayers().parallelStream().filter(p -> p.hasPermission(permission) ||
                p.hasPermission(Permissions.ADMIN_BYPASS.p())).forEach(p -> p.sendMessage(Text.of(prefix(), SpongeCommonUtils.fromAPI(message))));
    }

    @Override
    public void tell(MessageReceiver receiver, com.darwinreforged.server.core.chat.Text message) {
        if (receiver instanceof DarwinPlayer) {
            Sponge.getServer().getPlayer(((DarwinPlayer) receiver).getUniqueId())
                    .ifPresent(spp -> spp.sendMessage(Text.of(prefix(), SpongeCommonUtils.fromAPI(message))));
        } else if (receiver instanceof Console) {
            Sponge.getServer().getConsole().sendMessage(Text.of(prefix(), SpongeCommonUtils.fromAPI(message)));
        } else {
            DarwinServer.getLog().warn("Failed to get receiver for : " + receiver);
        }
    }

    @Override
    public void tellNoMarkup(MessageReceiver receiver, com.darwinreforged.server.core.chat.Text message) {
        if (receiver instanceof DarwinPlayer) {
            Sponge.getServer().getPlayer(((Target) receiver).getUniqueId()).ifPresent(spp -> spp.sendMessage(SpongeCommonUtils.fromAPI(message)));
        } else if (receiver instanceof Console) {
            Sponge.getServer().getConsole().sendMessage(SpongeCommonUtils.fromAPI(message));
        }
    }

    @Override
    public boolean isOnline(UUID uuid) {
        return Sponge.getServer().getPlayer(uuid).map(User::isOnline).orElse(false);
    }

    @Override
    public void kick(DarwinPlayer player) {
        Sponge.getServer().getPlayer(player.getUniqueId()).ifPresent(Player::kick);
    }

    @Override
    public boolean hasPermission(DarwinPlayer player, String permission) {
        if (permission.equals(Permissions.ADMIN_BYPASS.p())) DarwinServer.getLog().warn("Received direct permission check for global bypass for player '" + player.getName() + "'");
        return Sponge.getServer().getPlayer(player.getUniqueId()).map(u -> u.hasPermission(permission) || u.hasPermission(Permissions.ADMIN_BYPASS.p())).orElse(false);
    }

    @Override
    public Optional<DarwinLocation> getLocation(DarwinPlayer player) {
        return Sponge.getServer().getPlayer(player.getUniqueId()).map(p -> {
            Location<World> worldLocation = p.getLocation();
            DarwinWorld darwinWorld = new DarwinWorld(worldLocation.getExtent().getUniqueId(), worldLocation.getExtent().getName());
            Vector3d vector3d = new Vector3d(worldLocation.getX(), worldLocation.getY(), worldLocation.getZ());
            return Optional.of(new DarwinLocation(darwinWorld, vector3d));
        }).orElse(Optional.empty());
    }

    @Override
    public DarwinItem<?> getItemInHand(DarwinPlayer player, boolean primaryHand) {
        Optional<ItemStack> itemStack = Sponge.getServer().getPlayer(player.getUniqueId())
                .flatMap(spp -> spp.getItemInHand(primaryHand ? HandTypes.MAIN_HAND : HandTypes.OFF_HAND));
        return itemStack.map(DarwinItem::new).orElse(null);
    }

    @Override
    public GameModes getGameMode(DarwinPlayer player) {
        return Sponge.getServer().getPlayer(player.getUniqueId()).flatMap(sp -> sp.getGameModeData().get(Keys.GAME_MODE)).map(gm -> {
            try {
                return Enum.valueOf(GameModes.class, gm.getName().toUpperCase());
            } catch (NullPointerException | IllegalArgumentException e) {
                return GameModes.UNKNOWN;
            }
        }).orElse(GameModes.UNKNOWN);
    }

    @Override
    public void setGameMode(GameModes mode, DarwinPlayer player) {
        Sponge.getServer().getPlayer(player.getUniqueId()).ifPresent(sp -> {
            org.spongepowered.api.entity.living.player.gamemode.GameMode spMode;
            switch (mode) {
                case CREATIVE:
                case UNKNOWN:
                default:
                    spMode = org.spongepowered.api.entity.living.player.gamemode.GameModes.CREATIVE;
                    break;
                case SURVIVAL:
                    spMode = org.spongepowered.api.entity.living.player.gamemode.GameModes.SURVIVAL;
                    break;
                case SPECTATOR:
                    spMode = org.spongepowered.api.entity.living.player.gamemode.GameModes.SPECTATOR;
                    break;
                case ADVENTURE:
                    spMode = org.spongepowered.api.entity.living.player.gamemode.GameModes.ADVENTURE;
                    break;
            }
            sp.getGameModeData().set(Keys.GAME_MODE, spMode);
        });
    }

    @Override
    public void executeCmd(String cmd, Target target) {
        if (target instanceof DarwinPlayer) {
            Sponge.getServer().getPlayer(target.getUniqueId()).ifPresent(sp -> Sponge.getCommandManager().process(sp, cmd));
        } else if (target instanceof Console) {
            Sponge.getCommandManager().process(Sponge.getServer().getConsole(), cmd);
        } else {
            DarwinServer.getLog().warn("Tried executing '{}' as non-player source ({})\n", cmd, target.getClass());
        }
    }

    @Override
    public List<DarwinPlayer> getOnlinePlayers() {
        return Sponge.getServer().getOnlinePlayers().stream().map(sp -> super.getPlayer(sp.getUniqueId(), sp.getName())).collect(Collectors.toList());
    }

    @Override
    public Optional<DarwinPlayer> getPlayer(String player) {
        return Sponge.getServer().getPlayer(player).map(sp -> super.getPlayer(sp.getUniqueId(), sp.getName()));
    }

    @Override
    public void teleportPlayer(DarwinPlayer player, DarwinLocation loc) {
        Sponge.getServer().getPlayer(player.getUniqueId()).ifPresent(sp -> {
            try {
                sp.setLocation(getSpongeLocationFrom(loc, true));
            } catch (IllegalArgumentException e) {
                DarwinServer.error(String.format("Could not find world with UUID '%s'", loc.getWorld().getWorldUUID()));
            }
        });
    }



    @Override
    public void sendPagination(MessageReceiver receiver, Pagination pagination) {
        org.spongepowered.api.text.channel.MessageReceiver spongeReceiver = null;
        if (receiver.equals(Console.instance)) spongeReceiver = Sponge.getServer().getConsole();
        else if (receiver instanceof DarwinPlayer)
            spongeReceiver = Sponge.getServer().getPlayer(((DarwinPlayer) receiver).getUniqueId()).orElse(null);

        if (spongeReceiver != null) {
            PaginationList.Builder builder = PaginationList.builder();

            if (pagination.getPadding() != null) builder.padding(SpongeCommonUtils.fromAPI(pagination.getPadding()));
            if (pagination.getLinesPerPage() > -1) builder.linesPerPage(pagination.getLinesPerPage());
            if (pagination.getHeader() != null) builder.header(SpongeCommonUtils.fromAPI(pagination.getHeader()));
            if (pagination.getFooter() != null) builder.footer(SpongeCommonUtils.fromAPI(pagination.getFooter()));
            if (pagination.getTitle() != null) builder.title(SpongeCommonUtils.fromAPI(pagination.getTitle()));
            if (pagination.getContents() != null) {
                builder.contents(
                        pagination.getContents().stream().map(SpongeCommonUtils::fromAPI).collect(Collectors.toList())
                );
            }

            builder.build().sendTo(spongeReceiver);
        }
    }

    @Override
    public String getPlayerName(UUID uuid) {
        return Sponge.getServer().getPlayer(uuid).map(Player::getName).orElse(DefaultTranslations.UNKNOWN_PLAYER.s());
    }

}
