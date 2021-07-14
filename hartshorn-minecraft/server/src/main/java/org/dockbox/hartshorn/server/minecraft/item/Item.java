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

package org.dockbox.hartshorn.server.minecraft.item;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.i18n.common.Language;
import org.dockbox.hartshorn.api.i18n.text.Text;
import org.dockbox.hartshorn.api.keys.KeyHolder;
import org.dockbox.hartshorn.api.keys.PersistentDataHolder;
import org.dockbox.hartshorn.persistence.PersistentCapable;
import org.dockbox.hartshorn.server.minecraft.item.persistence.PersistentItemModel;
import org.dockbox.hartshorn.server.minecraft.players.Profile;
import org.jetbrains.annotations.NonNls;

import java.util.List;
import java.util.Set;

public interface Item extends KeyHolder<Item>, PersistentDataHolder, PersistentCapable<PersistentItemModel> {

    /**
     * @param id
     *         The fully qualified identifier of a block, e.g. {@code minecraft:stone}
     *
     * @return The item instance, or {@link ItemTypes#AIR}
     */
    static Item of(@NonNls String id) {
        // No need to filter air, can directly return it here.
        if (ItemTypes.AIR.id().equals(id)) return Hartshorn.context().get(Item.class, id);

        return Hartshorn.context().first(ItemContext.class)
                .map(context -> context.custom(id))
                .filter(item -> !item.isAir())
                .or(Hartshorn.context().get(Item.class, id));
    }

    static Item of(ItemTypes itemType) {
        return of(itemType.id());
    }

    boolean isAir();

    String id();

    Text displayName();

    Item displayName(Text displayName);

    Text displayName(Language language);

    List<Text> lore();

    Item lore(List<Text> lore);

    int amount();

    Item amount(int amount);

    void removeDisplayName();

    void addLore(Text lore);

    void removeLore();

    int stackSize();

    Set<Enchant> enchantments();

    void addEnchant(Enchant enchant);

    void removeEnchant(Enchant enchant);

    boolean isBlock();

    boolean isHead();

    Item profile(Profile profile);

    Item stack();

    Exceptional<String> category();
}
