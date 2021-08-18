package org.dockbox.hartshorn.headdatabase;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.exceptions.ApplicationException;
import org.dockbox.hartshorn.commands.annotations.Command;
import org.dockbox.hartshorn.di.annotations.service.Service;
import org.dockbox.hartshorn.events.annotations.Listener;
import org.dockbox.hartshorn.i18n.text.Text;
import org.dockbox.hartshorn.server.minecraft.events.server.EngineChangedState;
import org.dockbox.hartshorn.server.minecraft.events.server.ServerState.Loading;
import org.dockbox.hartshorn.server.minecraft.inventory.Element;
import org.dockbox.hartshorn.server.minecraft.inventory.InventoryLayout;
import org.dockbox.hartshorn.server.minecraft.inventory.InventoryType;
import org.dockbox.hartshorn.server.minecraft.inventory.builder.LayoutBuilder;
import org.dockbox.hartshorn.server.minecraft.inventory.pane.StaticPane;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.item.ItemTypes;
import org.dockbox.hartshorn.server.minecraft.players.Player;
import org.dockbox.hartshorn.server.minecraft.players.Profile;

import java.util.List;

import javax.inject.Inject;

@Service
public class HeadDatabaseService {

    @Inject
    private HeadDatabaseSource source;

    private final Element BACK = Element.of(ItemTypes.BARRIER.item().displayName(Text.of("$1Back")), ctx -> {
        this.open(ctx.player());
        return false;
    });

    @Command("heads")
    public void open(final Player player) {
        // Expected layout, slots indicated by index:
        // 0  1  2  3  4  5  6  7  8
        // 9  10 11 12 13 14 15 16 17
        // 18 19 20 21 22 23 24 25 26
        // 27 28 29 30 31 32 33 34 35
        final LayoutBuilder layout = InventoryLayout.builder(InventoryType.GENERIC_4_ROWS)
                .border(Item.of(ItemTypes.CYAN_STAINED_GLASS_PANE));

        int next = 11;
        for (final HeadDatabaseCategory category : HeadDatabaseCategory.values()) {
            final Element element = this.element(category);
            layout.set(element, next);
            // 10 categories, so we want 5 per row
            if (next == 15) next = 20;
            else next++;
        }

        final StaticPane pane = layout.toStaticPaneBuilder().lock(true).build();
        pane.open(player);
    }

    private void openCategory(final HeadDatabaseCategory category, final Player player) {
        // TODO GLieben, can we make elements use lazy loading?
        final List<Element> elements = this.source.get(category).stream()
                .map(this::item)
                .map(item -> Element.of(item, ctx -> {
                    ctx.player().inventory().give(item);
                    return false;
                })).toList();

        InventoryLayout.builder(InventoryType.GENERIC_5_ROWS)
                .toPaginatedPaneBuilder()
                .title(Text.of("$1" + category.displayName()))
                .elements(elements)
                .action(0, pane -> this.BACK)
                .lock(true)
                .build()
                .open(player);
    }

    private Element element(final HeadDatabaseCategory category) {
        final Exceptional<CustomHead> head = this.source.first(category);
        if (head.absent()) return Element.of(ItemTypes.AIR.item());

        final Item item = this.item(head.get()).displayName(Text.of("$1" + category.displayName()));
        return Element.of(item, ctx -> {
            this.openCategory(category, ctx.player());
            return false;
        });
    }

    private Item item(final CustomHead head) {
        final Profile profile = Profile.of(head.uuid()).property(head.property());
        return Item.of(ItemTypes.STEVE_HEAD)
                .profile(profile)
                .displayName(Text.of("$1" + head.name()));
    }

    @Listener
    public void on(final EngineChangedState<Loading> event) throws ApplicationException {
        this.source.collect();
    }

}
