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

package org.dockbox.hartshorn.playersettings;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.api.i18n.MessageReceiver;
import org.dockbox.hartshorn.api.i18n.common.Language;
import org.dockbox.hartshorn.api.i18n.common.ResourceEntry;
import org.dockbox.hartshorn.api.i18n.entry.FakeResource;
import org.dockbox.hartshorn.api.i18n.entry.Resource;
import org.dockbox.hartshorn.api.i18n.text.Text;
import org.dockbox.hartshorn.api.keys.PersistentDataHolder;
import org.dockbox.hartshorn.commands.annotations.Command;
import org.dockbox.hartshorn.di.annotations.service.Service;
import org.dockbox.hartshorn.playersettings.service.SettingsContext;
import org.dockbox.hartshorn.server.minecraft.inventory.Element;
import org.dockbox.hartshorn.server.minecraft.inventory.InventoryLayout;
import org.dockbox.hartshorn.server.minecraft.inventory.InventoryType;
import org.dockbox.hartshorn.server.minecraft.inventory.builder.PaginatedPaneBuilder;
import org.dockbox.hartshorn.server.minecraft.inventory.pane.PaginatedPane;
import org.dockbox.hartshorn.server.minecraft.inventory.pane.Pane;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.item.ItemTypes;
import org.dockbox.hartshorn.server.minecraft.players.Player;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.util.List;

@Service
public class PlayerSettings {

    public static final Setting<Boolean> RECEIVING_NOTIFICATIONS = Setting.of(Boolean.class)
            .resource(new Resource("Receive notifications", "settings.notifications"))
            .description(new Resource("Whether to receive server notifications, for example when you change your client-side language preferences.", "settings.notifications.description"))
            .owner(PlayerSettings.class)
            .converter(value -> value ? new Resource("Yes", "yes") : new Resource("No", "no"))
            .defaultValue(() -> true)
            .display(() -> Item.of(ItemTypes.INK_SAC))
            .action(PlayerSettings::toggleNotifications)
            .ok();

    public static final Setting<Integer> LANGUAGE = Setting.of(Integer.class)
            .from(Player.LANGUAGE)
            .resource(new Resource("Language", "settings.language"))
            .description(new Resource("The language in which messages are displayed to you.", "settings.language.description"))
            .owner(PlayerSettings.class)
            .converter(ordinal -> {
                Language language = Language.values()[ordinal];
                return new FakeResource(language.nameLocalized());
            })
            .defaultValue(Language.EN_US::ordinal)
            .display(() -> Item.of(ItemTypes.BOOK_AND_QUILL))
            .ok();

    private final List<Setting<?>> settings = HartshornUtils.emptyConcurrentList();

    private static void toggleNotifications(PersistentDataHolder holder) {
        final Boolean receiving = RECEIVING_NOTIFICATIONS.get(holder);
        holder.set(RECEIVING_NOTIFICATIONS, !receiving);
        if (holder instanceof MessageReceiver) {
            ((MessageReceiver) holder).send(Text.of("Receiving notifications: " + !receiving));
        }
    }

    @Command("settings")
    public void settings(Player source) {
        PaginatedPaneBuilder builder = InventoryLayout.builder(InventoryType.DOUBLE_CHEST)
                .toPaginatedPaneBuilder();
        builder.title(new Resource("$1Settings", "settings").translate(source).asText());
        final PaginatedPane pane = builder.build();

        final List<Element> content = Hartshorn.context().first(SettingsContext.class).get().settings().stream()
                .map(setting -> this.fromSetting(setting, source, pane))
                .toList();
        pane.elements(content);

        pane.open(source);
    }

    private Element fromSetting(Setting<?> setting, Player source, Pane build) {
        Item item = setting.item();
        this.modify(item, setting, source);
        return Element.of(item, player -> {
            setting.action().accept(player);
            this.settings(player);
        });
    }

    private void modify(Item item, Setting<?> setting, Player source) {
        item.displayName(Text.of("$1", setting.resource().translate(source), " $3(", this.value(setting, source), "$3)"));
        item.lore(HartshornUtils.singletonList(Text.of("$2", setting.description().translate(source))));
    }

    private <T> ResourceEntry value(Setting<T> setting, Player source) {
        final T o = setting.get(source);
        return setting.convert(o).translate(source);
    }
}
