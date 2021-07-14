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

package org.dockbox.hartshorn.sponge.inventory;

import net.kyori.adventure.text.Component;
import net.minecraft.world.item.CreativeModeTab;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.i18n.common.Language;
import org.dockbox.hartshorn.api.i18n.text.Text;
import org.dockbox.hartshorn.di.annotations.inject.Wired;
import org.dockbox.hartshorn.server.minecraft.item.Enchant;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.item.ReferencedItem;
import org.dockbox.hartshorn.server.minecraft.players.Profile;
import org.dockbox.hartshorn.sponge.game.SpongeComposite;
import org.dockbox.hartshorn.sponge.game.SpongeProfile;
import org.dockbox.hartshorn.sponge.util.SpongeConvert;
import org.dockbox.hartshorn.sponge.util.SpongeUtil;
import org.dockbox.hartshorn.util.HartshornUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.DataHolder.Mutable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SpongeItem extends ReferencedItem<ItemStack> implements SpongeComposite {

    public SpongeItem(@NotNull ItemStack reference) {
        super(reference);
    }

    @Wired
    public SpongeItem(String id) {
        super(id);
    }

    @Override
    public String id() {
        return SpongeUtil.location(this.item().map(ItemStack::type), RegistryTypes.ITEM_TYPE)
                .map(ResourceKey::asString)
                .or("");
    }

    @Override
    public boolean isAir() {
        if (this.id().equals(org.dockbox.hartshorn.server.minecraft.item.ItemTypes.AIR.id())) return true;
        else {
            return this.item()
                    .map(item -> item.isEmpty() || item.type() == ItemTypes.AIR.get())
                    .or(true);
        }
    }

    @Override
    public SpongeItem displayName(Text displayName) {
        this.item().present(item -> item.offer(Keys.CUSTOM_NAME, SpongeConvert.toSponge(displayName)));
        return this;
    }

    @Override
    public Text displayName(Language language) {
        return SpongeUtil.get(this.item(), Keys.CUSTOM_NAME, SpongeConvert::fromSponge, Text::of);
    }

    @Override
    public List<Text> lore() {
        return this.item()
                .map(item -> item.get(Keys.LORE)
                        .orElseGet(HartshornUtils::emptyList)
                        .stream()
                        .map(SpongeConvert::fromSponge)
                        .toList()
                ).orElse(HartshornUtils::emptyList)
                .get();
    }

    @Override
    public SpongeItem lore(List<Text> lore) {
        this.item().present(item -> {
            final List<Component> components = lore.stream()
                    .map(SpongeConvert::toSponge)
                    .map(Component::asComponent)
                    .toList();
            item.offer(Keys.LORE, components);
        });
        return this;
    }

    @Override
    public int amount() {
        return this.item().map(ItemStack::quantity).or(1);
    }

    @Override
    public SpongeItem amount(int amount) {
        this.item().present(item -> item.setQuantity(amount));
        return this;
    }

    @Override
    public void removeDisplayName() {
        this.item().present(item -> item.remove(Keys.CUSTOM_NAME));
    }

    @Override
    public void addLore(Text lore) {
        this.item().present(item -> {
            final List<Text> lines = this.lore();
            lines.add(lore);
            final List<Component> components = lines.stream()
                    .map(SpongeConvert::toSponge)
                    .map(Component::asComponent)
                    .toList();
            item.offer(Keys.LORE, components);
        });
    }

    @Override
    public void removeLore() {
        this.item().present(item -> item.remove(Keys.LORE));
    }

    @Override
    public int stackSize() {
        return this.item().map(ItemStack::maxStackQuantity).or(DEFAULT_STACK_SIZE);
    }

    @Override
    public Set<Enchant> enchantments() {
        return this.item()
                .map(item -> {
                    final List<Enchantment> applied = item.get(Keys.APPLIED_ENCHANTMENTS).orElseGet(HartshornUtils::emptyList);
                    final List<Enchantment> stored = item.get(Keys.STORED_ENCHANTMENTS).orElseGet(HartshornUtils::emptyList);
                    return HartshornUtils.merge(applied, stored).stream()
                            .map(SpongeConvert::fromSponge)
                            .filter(Exceptional::present)
                            .map(Exceptional::get)
                            .collect(Collectors.toSet());
                }).orElse(HartshornUtils::emptySet)
                .get();
    }

    @Override
    public void addEnchant(Enchant enchant) {
        this.item().present(item -> SpongeConvert.toSponge(enchant)
                .present(enchantment -> item.transform(Keys.APPLIED_ENCHANTMENTS, list -> {
                    list.add(enchantment);
                    return list;
                })));
    }

    @Override
    public void removeEnchant(Enchant enchant) {
        this.item().present(item -> SpongeConvert.toSponge(enchant)
                .present(enchantment -> item.transform(Keys.APPLIED_ENCHANTMENTS, list -> {
                    list.removeIf(e -> e.equals(enchantment));
                    return list;
                })));
    }

    @Override
    public boolean isBlock() {
        return this.item().map(item -> item.type().block().isPresent()).or(false);
    }

    @Override
    public boolean isHead() {
        return this.item().map(item -> item.type() == ItemTypes.PLAYER_HEAD.get()).or(false);
    }

    @Override
    public Item profile(Profile profile) {
        if (this.isHead() && profile instanceof SpongeProfile spongeProfile) {
            this.item().present(item -> item.offer(Keys.GAME_PROFILE, spongeProfile.profile()));
        }
        return this;
    }

    @Override
    public Exceptional<String> category() {
        return this.item()
                .map(net.minecraft.world.item.ItemStack.class::cast)
                .map(stack -> stack.getItem().getItemCategory())
                .map(CreativeModeTab::getRecipeFolderName);
    }

    @Override
    protected ItemStack from(String id) {
        ItemType type;
        if (id.indexOf(':') >= 0) {
            type = SpongeUtil.fromNamespacedRegistry(RegistryTypes.ITEM_TYPE, id).orNull();
        } else {
            type = SpongeUtil.fromMCRegistry(RegistryTypes.ITEM_TYPE, id).orNull();
        }
        if (type == null) return ItemStack.empty();

        return ItemStack.builder()
                .itemType(type)
                .build();
    }

    @Override
    public Exceptional<? extends Mutable> dataHolder() {
        return this.reference();
    }

    private Exceptional<ItemStack> item() {
        return this.reference();
    }
}
