package com.darwinreforged.server.modules.dave;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.chat.DiscordChatManager;
import com.darwinreforged.server.core.chat.Text;
import com.darwinreforged.server.core.files.FileManager;
import com.darwinreforged.server.modules.dave.DaveTrigger.Response;

import net.dv8tion.jda.api.entities.TextChannel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 The type Dave configuration util.
 */
@SuppressWarnings("unchecked")
public class DaveConfigurationUtil {

    private List<DaveTrigger> triggers = new ArrayList<>();
    private final List<UUID> muted = new ArrayList<>();
    private final TextChannel channel;
    private final Text prefix;
    private final String messageDefaultColor;

    /**
     Instantiates a new Dave configuration util.
     */
    public DaveConfigurationUtil() {
        FileManager fu = DarwinServer.getUtilMan().get(FileManager.class);

        // Load triggers
        File triggerFile = new File(fu.getDataDirectory(DaveChatModule.class).toFile(), "triggers.yml");
        TriggerConfig triggerData = fu.getYamlDataFromFile(triggerFile, TriggerConfig.class, null);
        if (triggerData != null) {
            this.triggers = triggerData.getTriggers();
        } else {
            List<DaveTrigger> triggers = Collections.singletonList(
                    new DaveTrigger(
                            "sample_id", Arrays.asList("sample,trigger", "second,trigger"),
                            false,
                            Arrays.asList(
                                    new Response("This is a chat message", "message"),
                                    new Response("https://example.com", "url"),
                                    new Response("help", "cmd")),
                            "dave.admin"));
            TriggerConfig triggerConfig = new TriggerConfig(triggers);
            fu.writeYamlDataToFile(triggerConfig, triggerFile);
        }

        // Load mutes
        File mutedFile = fu.createFileIfNotExists(new File(fu.getDataDirectory(DaveChatModule.class).toFile(), "muted.yml"));
        Map<String, Object> mutedData = fu.getYamlDataFromFile(mutedFile);
        List<String> mutedStrings = (List<String>) mutedData.getOrDefault("muted", Collections.emptyList());
        muted.addAll(mutedStrings.stream().map(UUID::fromString).collect(Collectors.toList()));

        // Load config
        Map<String, Object> daveConfig = fu.getYamlDataForConfig(DaveChatModule.class);
        boolean updateConfig = false;
        DiscordChatManager du = DarwinServer.getUtilMan().get(DiscordChatManager.class);
        String channelId;
        if (daveConfig.containsKey("channel")) {
            channelId = String.valueOf(daveConfig.get("channel"));
        } else {
            channelId = "123456789123456789";
            daveConfig.put("channel", channelId);
            updateConfig = true;
        }
        this.channel = du.getChannel(channelId);

        if (daveConfig.containsKey("prefix")) {
            this.prefix = Text.of(String.valueOf(daveConfig.get("prefix")));
        } else {
            this.prefix = Text.of("Dave : ");
            daveConfig.put("prefix", "Dave : ");
            updateConfig = true;
        }

        if (daveConfig.containsKey("messageColor")) {
            this.messageDefaultColor = String.valueOf(daveConfig.get("messageColor"));
        } else {
            this.messageDefaultColor = "&f";
            daveConfig.put("messageColor", "&f");
            updateConfig = true;
        }

        if (updateConfig) {
            fu.writeYamlDataForConfig(daveConfig, DaveChatModule.class);
        }
    }

    /**
     Gets triggers.

     @return the triggers
     */
    public List<DaveTrigger> getTriggers() {
        return triggers;
    }

    /**
     Gets muted.

     @return the muted
     */
    public List<UUID> getMuted() {
        return muted;
    }

    /**
     Gets channel.

     @return the channel
     */
    public TextChannel getChannel() {
        return channel;
    }

    /**
     Gets prefix.

     @return the prefix
     */
    public Text getPrefix() {
        return prefix;
    }

    /**
     Gets message default color.

     @return the message default color
     */
    public String getMessageDefaultColor() {
        return messageDefaultColor;
    }

    private static class TriggerConfig {
        private List<DaveTrigger> triggers;

        /**
         Instantiates a new Trigger config.
         */
        public TriggerConfig() {
        }

        /**
         Instantiates a new Trigger config.

         @param triggers
         the triggers
         */
        public TriggerConfig(List<DaveTrigger> triggers) {
            this.triggers = triggers;
        }

        /**
         Gets triggers.

         @return the triggers
         */
        public List<DaveTrigger> getTriggers() {
            return triggers;
        }

        /**
         Sets triggers.

         @param triggers
         the triggers
         */
        public void setTriggers(List<DaveTrigger> triggers) {
            this.triggers = triggers;
        }
    }
}
