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
import org.dockbox.hartshorn.api.events.annotations.Listener;
import org.dockbox.hartshorn.api.i18n.entry.Resource;
import org.dockbox.hartshorn.api.i18n.text.actions.HoverAction;
import org.dockbox.hartshorn.api.i18n.text.pagination.PaginationBuilder;
import org.dockbox.hartshorn.api.task.TaskRunner;
import org.dockbox.hartshorn.commands.RunCommandAction;
import org.dockbox.hartshorn.commands.annotations.Command;
import org.dockbox.hartshorn.commands.context.CommandContext;
import org.dockbox.hartshorn.commands.source.CommandSource;
import org.dockbox.hartshorn.commands.source.DiscordCommandSource;
import org.dockbox.hartshorn.dave.models.DaveTriggers;
import org.dockbox.hartshorn.di.annotations.inject.Wired;
import org.dockbox.hartshorn.di.context.ApplicationContext;
import org.dockbox.hartshorn.di.properties.InjectableType;
import org.dockbox.hartshorn.di.properties.InjectorProperty;
import org.dockbox.hartshorn.discord.events.DiscordChatReceivedEvent;
import org.dockbox.hartshorn.persistence.FileManager;
import org.dockbox.hartshorn.persistence.FileType;
import org.dockbox.hartshorn.persistence.FileTypeProperty;
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

@Command("dave")
public class Dave implements InjectableType {

    public static final Setting<Boolean> MUTED_DAVE = Setting.of(Boolean.class)
            .resource(new Resource("Muted Dave", "settings.dave.muted"))
            .description(new Resource("Whether you will see Dave's responses", "settings.dave.muted.description"))
            .owner(Dave.class)
            .converter(value -> value ? new Resource("Yes", "yes") : new Resource("No", "no"))
            .defaultValue(() -> false)
            .display(() -> Item.of(ItemTypes.BOTTLE_O_ENCHANTING))
            .ok();

    private static final int msTick = 20;
    private DaveTriggers triggers = new DaveTriggers();
    private DaveConfig config = new DaveConfig();

    public static final String DAVE_MUTE = "dave.mute";
    public static final String DAVE_REFRESH = "dave.refresh";
    public static final String DAVE_TRIGGERS = "dave.triggers.list";
    public static final String DAVE_TRIGGER_RUN = "dave.triggers.run";

    @Wired
    private DaveResources resources;

    @Wired
    private ApplicationContext context;

    @Command(value = "mute", permission = Dave.DAVE_MUTE)
    public void mute(Player player) {
        DaveUtils.toggleMute(player);
    }

    @Command(value = "triggers", permission = Dave.DAVE_TRIGGERS)
    public void triggers(CommandSource source) {
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
    public void run(Player source, CommandContext context) {
        String triggerId = context.get("trigger");
        this.triggers.find(triggerId)
                .present(trigger -> DaveUtils.performTrigger(source, source.name(), trigger, "", this.config))
                .absent(() -> source.send(this.resources.triggerNotfound(triggerId)));
    }

    @Command(value = "refresh", permission = Dave.DAVE_REFRESH)
    public void refresh(CommandSource source) {
        this.enable();
        source.sendWithPrefix(this.resources.reload());
    }

    @Override
    public void enable(InjectorProperty<?>... properties) {
        FileManager fm = this.context.get(FileManager.class, FileTypeProperty.of(FileType.YAML));
        Path triggerFile = fm.dataFile(Dave.class, "triggers");
        if (HartshornUtils.empty(triggerFile)) this.restoreTriggerFile(fm, triggerFile);

        fm.read(triggerFile, DaveTriggers.class).present(triggers -> {
            Hartshorn.log().info("Found " + triggers.triggers().size() + " triggers");
            this.triggers = triggers;
        }).caught(e -> Hartshorn.log().warn("Could not load triggers for Dave"));

        Path configFile = fm.configFile(Dave.class);
        fm.read(configFile, DaveConfig.class)
                .present(config -> this.config = config)
                .caught(e -> Hartshorn.log().warn("Could not load config for Dave"));
    }

    private void restoreTriggerFile(FileManager fm, Path triggerFile) {
        fm.copyDefaultFile("dave_trigger.yml", triggerFile);
    }

    @Listener
    public void on(EngineChangedState<Reload> event) {
        this.enable();
    }

    @Listener
    public void on(SendChatEvent sendChatEvent) {
        Player player = (Player) sendChatEvent.subject();
        DaveUtils.findMatching(this.triggers, sendChatEvent.message().toPlain()).present(trigger -> this.context.get(TaskRunner.class).acceptDelayed(() -> DaveUtils.performTrigger(
                player,
                player.name(),
                trigger,
                sendChatEvent.message().toPlain(),
                this.config),
                5 * msTick, TimeUnit.MILLISECONDS)
        );
    }

    @Listener
    public void on(DiscordChatReceivedEvent chatEvent) {
        if (chatEvent.channel().getId().equals(this.config.channel().getId())) {
            DaveUtils.findMatching(this.triggers, chatEvent.message().getContentRaw()).present(trigger -> this.context.get(TaskRunner.class).acceptDelayed(() -> DaveUtils.performTrigger(
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
