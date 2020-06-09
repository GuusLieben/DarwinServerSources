package com.darwinreforged.server.core.types.living;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.chat.Text;
import com.darwinreforged.server.core.commands.annotations.Command;
import com.darwinreforged.server.core.player.PlayerManager;
import com.darwinreforged.server.core.resources.Permissions;
import com.darwinreforged.server.core.resources.translations.Translation;

import java.util.Arrays;

/**
 The type Console.
 */
public class Console extends CommandSender {

    /**
     The constant instance.
     */
    public static final Console instance = new Console();

    private Console() {}

    @Override
    public void explainCommand(String message, Command command) {
        if (command != null) {
            sendMessage("> Usage : " + command.usage(), true);
            sendMessage("> Short description : " + command.desc(), true);
            sendMessage("> Error : " + message, true);
            sendMessage("> Permitted flags : " + Arrays.toString(command.flags()), true);
        }
    }

    @Override
    public boolean hasPermission(Permissions permission) {
        return true;
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public String getName(boolean lookup) {
        return getName();
    }

    @Override
    public void execute(String cmd) {
        DarwinServer.getUtilMan().get(PlayerManager.class).executeCmd(cmd, this);
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
        sendMessage(Text.of(message), plain);
    }

    @Override
    public void sendMessage(String message, Permissions permission, boolean plain) {
        sendMessage(Text.of(message), plain);
    }

    @Override
    public void sendMessage(Translation translation, String permission, boolean plain) {
        sendMessage(translation.s(), plain);
    }

    @Override
    public void sendMessage(Translation translation, Permissions permission, boolean plain) {
        sendMessage(translation.s(), plain);
    }

    @Override
    public void sendMessage(Text text, String permission, boolean plain) {
        sendMessage(text, plain);
    }

    @Override
    public void sendMessage(Text text, Permissions permission, boolean plain) {
        sendMessage(text, plain);
    }
}
