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

package org.dockbox.hartshorn.toolbinding;

import org.dockbox.hartshorn.i18n.text.Text;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.players.ClickType;
import org.dockbox.hartshorn.server.minecraft.players.Hand;
import org.dockbox.hartshorn.server.minecraft.players.Player;
import org.dockbox.hartshorn.server.minecraft.players.Sneaking;
import org.dockbox.hartshorn.util.HartshornUtils;
import org.jetbrains.annotations.NonNls;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemTool {

    private final Text name;
    private final List<Text> lore;
    private final BiConsumer<Player, Item> consumer;
    private final List<Predicate<ToolInteractionEvent>> filters;
    private final List<Consumer<Item>> modifiers;

    public ItemTool(
            Text name,
            List<Text> lore,
            BiConsumer<Player, Item> consumer,
            List<Predicate<ToolInteractionEvent>> filters,
            List<Consumer<Item>> modifiers
    ) {
        this.name = name;
        this.lore = lore;
        this.consumer = consumer;
        this.filters = filters;
        this.modifiers = modifiers;
    }

    public static void reset(Item item) {
        item.removeDisplayName();
        item.removeLore();
    }

    public static ToolBuilder builder() {
        return new ToolBuilder();
    }

    public boolean accepts(ToolInteractionEvent event) {
        return this.filters.stream().allMatch(predicate -> predicate.test(event));
    }

    public void perform(Player player, Item item) {
        this.consumer.accept(player, item);
    }

    public void prepare(Item item) {
        if (null != this.name) item.displayName(this.name);
        if (null != this.lore) item.lore(this.lore);
        this.modifiers.forEach(modifiers -> modifiers.accept(item));
    }

    @SuppressWarnings("unused")
    public static final class ToolBuilder {
        private final List<Predicate<ToolInteractionEvent>> filters = HartshornUtils.emptyConcurrentList();
        private final List<Consumer<Item>> modifiers = HartshornUtils.emptyConcurrentList();
        private BiConsumer<Player, Item> consumer;
        private Text name;
        private List<Text> lore;

        private ToolBuilder() {}

        public ToolBuilder perform(BiConsumer<Player, Item> consumer) {
            this.consumer = consumer;
            return this;
        }

        public ToolBuilder resetFilters() {
            this.filters.clear();
            return this;
        }

        public ToolBuilder only(Player player) {
            this.filters.add(e -> e.player().equals(player));
            return this;
        }

        public ToolBuilder only(UUID playerId) {
            this.filters.add(e -> e.player().uniqueId().equals(playerId));
            return this;
        }

        public ToolBuilder only(@NonNls String player) {
            this.filters.add(e -> e.player().name().equals(player));
            return this;
        }

        public ToolBuilder only(Sneaking sneaking) {
            this.filters.add(e -> Sneaking.EITHER == sneaking || sneaking == e.sneaking());
            return this;
        }

        public ToolBuilder only(ClickType clickType) {
            this.filters.add(e -> ClickType.EITHER == clickType || clickType == e.type());
            return this;
        }

        public ToolBuilder only(Hand hand) {
            this.filters.add(e -> Hand.EITHER == hand || hand == e.hand());
            return this;
        }

        public ToolBuilder only(Predicate<ToolInteractionEvent> predicate) {
            this.filters.add(predicate);
            return this;
        }

        public ToolBuilder name(Text name) {
            this.name = name;
            return this;
        }

        public ToolBuilder lore(List<Text> lore) {
            this.lore = lore;
            return this;
        }

        public ToolBuilder modify(Consumer<Item> modifier) {
            this.modifiers.add(modifier);
            return this;
        }

        public ItemTool build() {
            return new ItemTool(this.name, this.lore, this.consumer, this.filters, this.modifiers);
        }
    }
}
