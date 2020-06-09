package com.darwinreforged.server.core.player;

import com.boydti.fawe.Fawe;
import com.boydti.fawe.object.FawePlayer;
import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.chat.Text;
import com.darwinreforged.server.core.commands.annotations.Command;
import com.darwinreforged.server.core.player.state.GameModes;
import com.darwinreforged.server.core.resources.Permissions;
import com.darwinreforged.server.core.resources.translations.DefaultTranslations;
import com.darwinreforged.server.core.resources.translations.Translation;
import com.darwinreforged.server.core.types.living.CommandSender;
import com.darwinreforged.server.core.types.location.DarwinLocation;
import com.darwinreforged.server.core.types.location.DarwinWorld;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 The type Darwin player.
 */
public class DarwinPlayer extends CommandSender {

    @Nullable
    private FawePlayer<?> fawePlayer;

    /**
     Instantiates a new Darwin player.

     @param uuid
     the uuid
     @param name
     the name
     */
    protected DarwinPlayer(UUID uuid, String name) {
        super.uuid = uuid;
        super.name = name;
        this.fawePlayer = Fawe.get().getCachedPlayer(uuid);
    }

    public Optional<FawePlayer<?>> getFawePlayer() {
        if (this.fawePlayer == null) this.fawePlayer = Fawe.get().getCachedPlayer(super.uuid);
        return Optional.ofNullable(this.fawePlayer);
    }

    /**
     Is online boolean.

     @return the boolean
     */
    public boolean isOnline() {
        return DarwinServer.getUtilMan().get(PlayerManager.class).isOnline(this);
    }

    /**
     Tell if permitted.

     @param text
     the text
     @param permission
     the permission
     */
    public void tellIfPermitted(String text, String permission, boolean plain) {
        if (hasPermission(permission)) sendMessage(text, plain);
    }

    /**
     Kick.
     */
    public void kick() {
        DarwinServer.getUtilMan().get(PlayerManager.class).kick(this);
    }

    public boolean hasPermission(Permissions permission) {
        return hasPermission(permission.p());
    }

    /**
     Has permission boolean.

     @param permission
     the permission

     @return the boolean
     */
    public boolean hasPermission(String permission) {
        return DarwinServer.getUtilMan().get(PlayerManager.class).hasPermission(this, permission);
    }

    /**
     Gets location.

     @return the location
     */
    public Optional<DarwinLocation> getLocation() {
        return DarwinServer.getUtilMan().get(PlayerManager.class).getLocation(this);
    }

    /**
     Gets world.

     @return the world
     */
    public Optional<DarwinWorld> getWorld() {
        return getLocation().map(DarwinLocation::getWorld);
    }

    /**
     Gets game mode.

     @return the game mode
     */
    public GameModes getGameMode() {
        return DarwinServer.getUtilMan().get(PlayerManager.class).getGameMode(this);
    }

    /**
     Sets game mode.

     @param mode
     the mode
     */
    public void setGameMode(GameModes mode) {
        DarwinServer.getUtilMan().get(PlayerManager.class).setGameMode(mode, this);
    }

    @Override
    public String getName(boolean lookup) {
        return DarwinServer.getUtilMan().get(PlayerManager.class).getPlayerName(this.uuid);
    }

    @Override
    public void execute(String cmd) {
        DarwinServer.getUtilMan().get(PlayerManager.class).executeCmd(cmd, this);
    }

    @Override
    public void explainCommand(String message, Command command) {
        sendMessage(message, false);
        if (command != null) {
            boolean flagEmpty = (command.flags().length == 0 || command.flags()[0].equals(""));
            sendMessage(DefaultTranslations.CMD_USAGE_TITLE.f(command.aliases()[0]), true);
            sendMessage(DefaultTranslations.CMD_USAGE.f(command.usage()), true);
            if (!flagEmpty) sendMessage(DefaultTranslations.CMD_FLAGS.f(String.join(", ", command.flags())), true);
            sendMessage(DefaultTranslations.CMD_DESCRIPTION.f(String.join(", ", command.desc())), true);
        }
    }

    @Override
    public void sendMessage(String message, boolean plain) {
        sendMessage(Text.of(message), plain);
    }

    @Override
    public void sendMessage(Translation translation, boolean plain) {
        sendMessage(translation.s(), plain);
    }

    @Override
    public void sendMessage(Text text, boolean plain) {
        PlayerManager man = DarwinServer.getUtilMan().get(PlayerManager.class);
        if (plain) man.tellNoMarkup(this, text);
        else man.tell(this, text);
    }

    @Override
    public void sendMessage(String message, String permission, boolean plain) {
        sendMessage(Text.of(message), permission, plain);
    }

    @Override
    public void sendMessage(String message, Permissions permission, boolean plain) {
        sendMessage(message, permission.p(), plain);
    }

    @Override
    public void sendMessage(Translation translation, String permission, boolean plain) {
        sendMessage(translation.s(), permission, plain);
    }

    @Override
    public void sendMessage(Translation translation, Permissions permission, boolean plain) {
        sendMessage(translation.s(), permission.p(), plain);
    }

    @Override
    public void sendMessage(Text text, String permission, boolean plain) {
        if (hasPermission(permission)) sendMessage(text, plain);
    }

    @Override
    public void sendMessage(Text text, Permissions permission, boolean plain) {
        sendMessage(text, permission.p(), plain);
    }

    public void teleport(DarwinLocation loc) {
        DarwinServer.getUtilMan().get(PlayerManager.class).teleportPlayer(this, loc);
    }
}
