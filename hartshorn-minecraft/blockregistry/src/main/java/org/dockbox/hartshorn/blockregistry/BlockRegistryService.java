package org.dockbox.hartshorn.blockregistry;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.api.i18n.text.Text;
import org.dockbox.hartshorn.commands.annotations.Command;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.players.Hand;
import org.dockbox.hartshorn.server.minecraft.players.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Command({"blockregistry", "registry"})
public class BlockRegistryService
{
    private final BlockRegistryManager blockRegistryManager = Hartshorn.context().get(BlockRegistryManager.class);

    @Command(value = "variant", arguments = "[item{Item}]", permission = Hartshorn.GLOBAL_PERMITTED)
    public void determineVariant(Player player, @Nullable Item item) {
        if (null == item)
            item = player.itemInHand(Hand.MAIN_HAND);
        if (null == item) {
            player.send(Text.of("You need to have an item in your main hand or specify an id"));
            return;
        }
        final String id = item.id();
        this.blockRegistryManager.variant(id)
            .present(v -> player.send(Text.of("The variant of", id, "is", v)))
            .absent(() -> player.send(Text.of("There doesn't appear to be a registered variant for", id)));
    }

    @Command(value = "add alias", arguments = "<alias> <root{Item}>", permission = "blockregistry.admin.add.alias")
    public void addAlias(Player player, @NotNull String alias, @NotNull Item root) {
        if (this.blockRegistryManager.hasAliasRegistered(alias)) {
            player.send(Text.of("The alias", alias, "has already been registered for another block"));
            return;
        }

        this.blockRegistryManager.addAlias(root.id(), alias);
        player.send(Text.of("Successfully added the alias", alias, "for the item", root.id()));
    }

    @Command(value = "add variant", arguments = "<root{Item}> <variant{VariantIdentifier}>, <family{Item}>",
             permission = "blockregistry.admin.add.variant")
    public void addVariant(Player player, @NotNull Item root, @NotNull VariantIdentifier variant, @NotNull Item family) {
        this.blockRegistryManager.variant(root)
            .present(variantIdentifier -> player.send(
                Text.of("The variant", root.id(), "is already registered as a ", variantIdentifier)))
            .absent(() -> {
                this.blockRegistryManager.addVariant(family.id(), variant, root.id());
                player.send(
                    Text.of("Successfully registered the variant", root.id(), "as a", variant, "in the",
                                 family.id(), "family"));
            });
    }
}
