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

package org.dockbox.hartshorn.dave;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.api.exceptions.ApplicationException;
import org.dockbox.hartshorn.api.task.TaskRunner;
import org.dockbox.hartshorn.commands.CommandSource;
import org.dockbox.hartshorn.commands.RunCommandAction;
import org.dockbox.hartshorn.commands.annotations.Command;
import org.dockbox.hartshorn.commands.context.CommandContext;
import org.dockbox.hartshorn.dave.models.DaveTriggers;
import org.dockbox.hartshorn.di.binding.Bindings;
import org.dockbox.hartshorn.di.context.ApplicationContext;
import org.dockbox.hartshorn.di.properties.AttributeHolder;
import org.dockbox.hartshorn.discord.DiscordCommandSource;
import org.dockbox.hartshorn.discord.events.DiscordChatReceivedEvent;
import org.dockbox.hartshorn.events.annotations.Listener;
import org.dockbox.hartshorn.i18n.entry.Resource;
import org.dockbox.hartshorn.i18n.text.actions.HoverAction;
import org.dockbox.hartshorn.i18n.text.pagination.PaginationBuilder;
import org.dockbox.hartshorn.persistence.FileManager;
import org.dockbox.hartshorn.persistence.FileType;
import org.dockbox.hartshorn.persistence.FileTypeAttribute;
import org.dockbox.hartshorn.playersettings.Setting;
import org.dockbox.hartshorn.server.minecraft.events.chat.SendChatEvent;
import org.dockbox.hartshorn.server.minecraft.events.server.EngineChangedState;
import org.dockbox.hartshorn.server.minecraft.events.server.ServerState.Reload;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.item.ItemTypes;
import org.dockbox.hartshorn.server.minecraft.players.Player;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

@Command("dave")
public class Dave implements AttributeHolder {

    public static final Setting<Boolean> MUTED_DAVE = Setting.of(Boolean.class)
            .id("dave.muted")
            .resource(ctx -> new Resource(ctx, "Muted Dave", "settings.dave.muted"))
            .description(ctx -> new Resource(ctx, "Whether you will see Dave's responses", "settings.dave.muted.description"))
            .owner(Dave.class)
            .converter((ctx, value) -> value ? new Resource(ctx, "Yes", "yes") : new Resource(ctx, "No", "no"))
            .defaultValue(() -> false)
            .display(ctx -> Item.of(ctx, ItemTypes.BOTTLE_O_ENCHANTING))
            .ok();
    public static final String DAVE_MUTE = "dave.mute";
    public static final String DAVE_REFRESH = "dave.refresh";
    public static final String DAVE_TRIGGERS = "dave.triggers.list";
    public static final String DAVE_TRIGGER_RUN = "dave.triggers.run";
    private static final int msTick = 20;
    private DaveTriggers triggers = new DaveTriggers();
    private DaveConfig config = new DaveConfig();

    @Inject private DaveResources resources;
    @Inject private DaveUtils utils;
    @Inject private ApplicationContext context;

    @Command(value = "mute", permission = Dave.DAVE_MUTE)
    public void mute(final Player player) {
        this.utils.toggleMute(player);
    }

    @Command(value = "triggers", permission = Dave.DAVE_TRIGGERS)
    public void triggers(final CommandSource source) {
        this.context.get(PaginationBuilder.class)
                .title(this.resources.triggerHeader().asText())
                .content(this.triggers.triggers().stream()
                        .map(trigger -> this.resources.triggerSingle(HartshornUtils.shorten(trigger.responses().get(0).message(), 75) + " ...")
                                .asText()
                                .onHover(HoverAction.showText(this.resources.triggerSingleHover().asText()))
                                .onClick(RunCommandAction.runCommand("/dave run " + trigger.id())))
                        .toList())
                .build()
                .send(source);
    }

    @Command(value = "run", arguments = "<trigger{String}>", permission = Dave.DAVE_TRIGGER_RUN)
    public void run(final Player source, final CommandContext context) {
        final String triggerId = context.get("trigger");
        this.triggers.find(triggerId)
                .present(trigger -> this.utils.performTrigger(source, source.name(), trigger, "", this.config))
                .absent(() -> source.send(this.resources.triggerNotfound(triggerId)));
    }

    @Command(value = "refresh", permission = Dave.DAVE_REFRESH)
    public void refresh(final CommandSource source) throws ApplicationException {
        Bindings.enable(this);
        source.sendWithPrefix(this.resources.reload());
    }

    @Override
    public void enable() {
        final FileManager fm = this.context.get(FileManager.class, FileTypeAttribute.of(FileType.YAML));
        final Path triggerFile = fm.dataFile(Dave.class, "triggers");
        if (HartshornUtils.empty(triggerFile)) this.restoreTriggerFile(fm, triggerFile);

        fm.read(triggerFile, DaveTriggers.class).present(triggers -> {
            Hartshorn.log().info("Found " + triggers.triggers().size() + " triggers");
            this.triggers = triggers;
        }).caught(e -> Hartshorn.log().warn("Could not load triggers for Dave"));

        final Path configFile = fm.configFile(Dave.class);
        fm.read(configFile, DaveConfig.class)
                .present(config -> this.config = config)
                .caught(e -> Hartshorn.log().warn("Could not load config for Dave"));
    }

    private void restoreTriggerFile(final FileManager fm, final Path triggerFile) {
        fm.copyDefaultFile("dave_trigger.yml", triggerFile);
    }

    @Listener
    public void on(final EngineChangedState<Reload> event) throws ApplicationException {
        Bindings.enable(this);
    }

    @Listener
    public void on(final SendChatEvent sendChatEvent) {
        final Player player = (Player) sendChatEvent.subject();
        this.utils.findMatching(this.triggers, sendChatEvent.message().toPlain()).present(trigger -> this.context.get(TaskRunner.class).acceptDelayed(() -> this.utils.performTrigger(
                        player,
                        player.name(),
                        trigger,
                        sendChatEvent.message().toPlain(),
                        this.config),
                5 * msTick, TimeUnit.MILLISECONDS)
        );
    }

    @Listener
    public void on(final DiscordChatReceivedEvent chatEvent) {
        if (chatEvent.channel().getId().equals(this.config.channel(chatEvent.applicationContext()).getId())) {
            this.utils.findMatching(this.triggers, chatEvent.message().getContentRaw()).present(trigger -> this.context.get(TaskRunner.class).acceptDelayed(() -> this.utils.performTrigger(
                            this.context.get(DiscordCommandSource.class, chatEvent.channel()),
                            chatEvent.author().getName(),
                            trigger,
                            chatEvent.message().getContentRaw(),
                            this.config),
                    5 * msTick, TimeUnit.MILLISECONDS)
            );
        }
    }
}
