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

package org.dockbox.hartshorn.test.objects;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.i18n.common.Language;
import org.dockbox.hartshorn.api.i18n.text.Text;
import org.dockbox.hartshorn.di.annotations.inject.Wired;
import org.dockbox.hartshorn.server.minecraft.item.Enchant;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.item.ItemTypes;
import org.dockbox.hartshorn.server.minecraft.item.SimplePersistentItemModel;
import org.dockbox.hartshorn.server.minecraft.item.persistence.PersistentItemModel;
import org.dockbox.hartshorn.server.minecraft.players.Profile;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

public class JUnitItem implements Item, JUnitPersistentDataHolder {

    @Getter
    private final String id;
    private final UUID persistentDataId = UUID.randomUUID();
    private final Set<Enchant> enchants = HartshornUtils.emptySet();
    @Getter
    private final List<Text> lore = HartshornUtils.emptyList();

    @Getter
    private Profile profile;
    @Getter @Setter
    private int amount = 1;
    @Getter
    private Text displayName;
    private boolean treatAsBlock = false;

    @Wired
    @Deprecated
    public JUnitItem(String id, int meta) {
        this.id = id;
        this.displayName = Text.of(id);
    }

    @Wired
    public JUnitItem(String id) {
        this.id = id;
        this.displayName = Text.of(id);
    }

    @Override
    public boolean isAir() {
        return this.id().equals(Item.of(ItemTypes.AIR).id());
    }

    @Override
    public Item displayName(Text displayName) {
        this.displayName = displayName;
        return this;
    }

    @Override
    public Text displayName(Language language) {
        return this.displayName;
    }

    @Override
    public JUnitItem lore(List<Text> lore) {
        this.lore.clear();
        this.lore.addAll(lore);
        return this;
    }

    @Override
    public void removeDisplayName() {
        this.displayName = Text.of(this.id());
    }

    @Override
    public void addLore(Text lore) {
        this.lore.add(lore);
    }

    @Override
    public void removeLore() {
        this.lore.clear();
    }

    @Override
    public Set<Enchant> enchantments() {
        return HartshornUtils.asUnmodifiableSet(this.enchants);
    }

    @Override
    public void addEnchant(Enchant enchant) {
        this.enchants.add(enchant);
    }

    @Override
    public void removeEnchant(Enchant enchant) {
        this.enchants.remove(enchant);
    }

    @Override
    public boolean isBlock() {
        return this.treatAsBlock;
    }

    public Item treatAsBlock() {
        this.treatAsBlock = true;
        return this;
    }

    @Override
    public Item profile(Profile profile) {
        if (this.isHead()) {
            this.profile = profile;
        }
        return this;
    }

    @Override
    public boolean isHead() {
        return Item.of(ItemTypes.SKELETON_SKULL).id().equals(this.id());
    }

    @Override
    public Item stack() {
        this.amount(this.stackSize());
        return this;
    }

    @Override
    public Exceptional<String> category() {
        return Exceptional.empty();
    }

    @Override
    public int stackSize() {
        return 64;
    }

    @Override
    public UUID uniqueId() {
        return this.persistentDataId;
    }

    @Override
    public String name() {
        return this.displayName().toString();
    }

    @Override
    public Class<? extends PersistentItemModel> modelType() {
        return SimplePersistentItemModel.class;
    }

    @Override
    public PersistentItemModel model() {
        return new SimplePersistentItemModel(this);
    }
}
