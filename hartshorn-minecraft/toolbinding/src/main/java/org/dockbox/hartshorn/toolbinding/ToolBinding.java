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

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.events.annotations.Listener;
import org.dockbox.hartshorn.api.events.annotations.Posting;
import org.dockbox.hartshorn.api.keys.Keys;
import org.dockbox.hartshorn.api.keys.PersistentDataKey;
import org.dockbox.hartshorn.api.keys.RemovableKey;
import org.dockbox.hartshorn.api.keys.TransactionResult;
import org.dockbox.hartshorn.di.annotations.inject.Wired;
import org.dockbox.hartshorn.di.annotations.service.Service;
import org.dockbox.hartshorn.server.minecraft.events.player.interact.PlayerInteractEvent;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.players.Sneaking;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.util.Map;
import java.util.UUID;

@Service
@Posting(ToolInteractionEvent.class)
public class ToolBinding {

    @Wired
    private ToolBindingResources resources;

    static final PersistentDataKey<String> PERSISTENT_TOOL = Keys.persistent(String.class, "Tool Binding", ToolBinding.class);
    private static ToolBinding instance;

    public static final RemovableKey<Item, ItemTool> TOOL_REMOVABLE_KEY = Keys.builder(Item.class, ItemTool.class)
            .withSetter((item, tool) -> instance.tool(item, tool))
            .withGetterSafe(item -> instance.get(item))
            .withRemover(item -> instance.removeTool(item))
            .build();

    private final Map<String, ItemTool> registry = HartshornUtils.emptyConcurrentMap();

    public ToolBinding() {
        instance = this;
    }

    private TransactionResult tool(Item item, ItemTool tool) {
        if (item.isBlock()) return TransactionResult.fail(this.resources.blockError());
        if (item.isAir()) return TransactionResult.fail(this.resources.handError());
        if (item.get(PERSISTENT_TOOL).present()) return TransactionResult.fail(this.resources.duplicateError());

        String bindingId = UUID.randomUUID().toString();

        TransactionResult result = item.set(PERSISTENT_TOOL, bindingId);
        if (result.successful()) {
            this.registry.put(bindingId, tool);
            tool.prepare(item);
        }

        return result;
    }

    private void removeTool(Item item) {
        Exceptional<String> identifier = item.get(PERSISTENT_TOOL);
        if (identifier.absent()) return;

        Exceptional<ItemTool> tool = this.get(item);
        if (tool.absent()) return;

        item.remove(PERSISTENT_TOOL);
        this.registry.remove(identifier.get());
        ItemTool.reset(item);
    }

    private Exceptional<ItemTool> get(Item item) {
        Exceptional<String> identifier = item.get(PERSISTENT_TOOL);
        if (identifier.absent()) return Exceptional.empty();

        String registryIdentifier = identifier.get();
        if (!this.registry.containsKey(registryIdentifier)) return Exceptional.empty();
        ItemTool itemTool = this.registry.get(registryIdentifier);

        return Exceptional.of(itemTool);
    }

    @Listener
    public void on(PlayerInteractEvent event) {
        Item itemInHand = event.subject().itemInHand(event.hand());
        if (itemInHand.isAir() || itemInHand.isBlock()) return;

        Exceptional<String> identifier = itemInHand.get(PERSISTENT_TOOL);
        if (identifier.absent()) return;
        if (!this.registry.containsKey(identifier.get())) return;
        ItemTool tool = this.registry.get(identifier.get());

        ToolInteractionEvent toolInteractionEvent = new ToolInteractionEvent(
                event.subject(),
                itemInHand,
                tool,
                event.hand(),
                event.clickType(),
                event.subject().sneaking() ? Sneaking.SNEAKING : Sneaking.STANDING);

        if (tool.accepts(toolInteractionEvent)) {
            toolInteractionEvent.post();
            if (toolInteractionEvent.cancelled()) return;
            tool.perform(event.subject(), itemInHand);
            event.cancelled(true); // To prevent block/entity damage
        }
    }
}
