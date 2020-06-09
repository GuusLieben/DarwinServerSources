package com.darwinreforged.server.modules.todo.dave;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.chat.ClickEvent;
import com.darwinreforged.server.core.chat.ClickEvent.ClickAction;
import com.darwinreforged.server.core.chat.DiscordChatManager;
import com.darwinreforged.server.core.chat.HoverEvent;
import com.darwinreforged.server.core.chat.HoverEvent.HoverAction;
import com.darwinreforged.server.core.chat.Text;
import com.darwinreforged.server.core.events.internal.chat.DiscordChatEvent;
import com.darwinreforged.server.core.events.internal.chat.SendChatMessageEvent;
import com.darwinreforged.server.core.events.util.Listener;
import com.darwinreforged.server.core.player.DarwinPlayer;
import com.darwinreforged.server.core.player.PlayerManager;
import com.darwinreforged.server.core.types.living.CommandSender;
import com.darwinreforged.server.core.types.living.Console;
import com.darwinreforged.server.core.util.CommonUtils;
import com.darwinreforged.server.modules.todo.dave.DaveTrigger.Response;

import net.dv8tion.jda.api.entities.TextChannel;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.SECONDS;

public class DaveChatListeners {

    private Text botPrefix;
    private String botDefaultColor;

    @Listener
    public void onDiscordChat(DiscordChatEvent event) {
        beforeEach();
        String playername = event.getMember().getEffectiveName();
        String message = event.getMessage().getContentRaw();
        String guild = event.getGuild().getId();

        if (guild.equals("341249512586608640") && event
                .getChannel()
                .getName()
                .equals("global")) {
            Console console = Console.instance;
            Executor.beforeExecution(playername, message, botPrefix, botDefaultColor, console, true);
            DarwinServer.getUtilMan().get(CommonUtils.class).scheduler()
                    .execute(new Executor())
                    .delayTicks(5)
                    .submit();
        }
    }

    private void beforeEach() {
        Optional<DaveChatModule> chatModuleOptional = DarwinServer.getModMan().getModule(DaveChatModule.class);
        if (chatModuleOptional.isPresent()) {
            botPrefix = chatModuleOptional.get().getConfigurationUtil().getPrefix();
            botDefaultColor = chatModuleOptional.get().getConfigurationUtil().getMessageDefaultColor().replaceAll("&", "\u00A7");
        }
    }

    @Listener
    public void onGameChat(SendChatMessageEvent event) {
        beforeEach();
        String playername = event.getTarget().getName();

        if (event.isGlobalChat())
            if (!(event.getTarget() instanceof Console)) {
                Executor.beforeExecution(playername, event.getMessage(), botPrefix, botDefaultColor, (DarwinPlayer) event.getTarget(), false);
                DarwinServer.getUtilMan().get(CommonUtils.class).scheduler()
                        .execute(new Executor())
                        .delayTicks(5)
                        .submit();
            }
    }

    public static class Executor
            implements Runnable {

        static String playername;
        static String message;
        static Text botPrefix;
        static String color;
        static CommandSender sender;
        static HashMap<DaveTrigger, LocalDateTime> timeSinceTrigger = new HashMap<>();
        static boolean isDiscordSource;

        static void beforeExecution(String playername, String message, Text botPrefix, String color, CommandSender sender, boolean isDiscordSource) {
            Executor.playername = playername;
            Executor.message = message;
            Executor.botPrefix = botPrefix;
            Executor.color = color;
            Executor.sender = sender;
            Executor.isDiscordSource = isDiscordSource;
        }

        @Override
        public void run() {
            DaveTrigger trigger = DaveRawUtils.getAssociatedTrigger(message);

            LocalDateTime timeOfLastTriggered = timeSinceTrigger.get(trigger);
            long secondsSinceLastResponse = 10;
            if (timeOfLastTriggered != null)
                secondsSinceLastResponse = timeOfLastTriggered.until(LocalDateTime.now(), SECONDS);

            if (trigger != null && secondsSinceLastResponse >= 10) handleTrigger(trigger);

        }

        public static void handleTrigger(DaveTrigger trigger) {
            String perm = trigger.getPermission();

            if (perm != null) {
                if (isDiscordSource) return;
                else {
                    PlayerManager pm = DarwinServer.getUtilMan().get(PlayerManager.class);
                    Optional<DarwinPlayer> dp = pm.getPlayer(playername);
                    if (dp.isPresent() && !dp.get().hasPermission(perm)) return;
                }
            }

            List<Response> responses = trigger.getResponses();

            boolean important = trigger.isImportant();

            responses.forEach(response -> {
                if (response.getType().equals("cmd")) {
                    executeCommand(response.getMessage());
                } else if (response.getType().equals("url")) {
                    printResponse(DaveRawUtils.parseWebsiteLink(response.getMessage()), true, important);
                } else {
                    printResponse(DaveRawUtils.parsePlaceHolders(message, response.getMessage(), playername), false, important);
                }
            });

            timeSinceTrigger.put(trigger, LocalDateTime.now());
        }

        private static void executeCommand(String command) {
            if (command.startsWith("*")) Console.instance.execute(command);
            else sender.execute(command);
        }

        private static void printResponse(String response, boolean link, boolean important) {
            Text message = Text.of(botPrefix.getText());
            message.append(color);

            if (link) {
                message.append(Text.of(DaveTranslations.DAVE_LINK_SUGGESTION.f(response)));
                message.setClickEvent(new ClickEvent(ClickAction.OPEN_URL, response));
                message.setHoverEvent(new HoverEvent(HoverAction.SHOW_TEXT, DaveTranslations.DAVE_LINK_SUGGESTION_HOVER.f(response)));
            } else message.append(response);

            PlayerManager pu = DarwinServer.getUtilMan().get(PlayerManager.class);

            DarwinServer.getModMan().getModule(DaveChatModule.class).ifPresent(dave -> {
                // Regular chat module
                pu.getOnlinePlayers().stream()
                        .filter(op -> !dave.getPlayerWhoMutedDave().contains(op.getUniqueId()) || important)
                        .forEach(op -> op.sendMessage(message, true));

                // Discord module
                TextChannel discordChannel = dave.getConfigurationUtil().getChannel();
                String discordMessage = response.replaceAll("§", "&");
                for (String regex : new String[]{"(&)([a-f])+", "(&)([0-9])+", "&l", "&n", "&o", "&k", "&m", "&r"})
                    discordMessage = discordMessage.replaceAll(regex, "");

                DarwinServer.getUtilMan().get(DiscordChatManager.class).sendToChannel(DaveTranslations.DAVE_DISCORD_FORMAT.f(discordMessage), discordChannel.getId());
            });
        }
    }
}
