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
import org.dockbox.hartshorn.api.domain.TypedOwner;
import org.dockbox.hartshorn.api.i18n.common.ResourceEntry;
import org.dockbox.hartshorn.api.i18n.entry.FakeResource;
import org.dockbox.hartshorn.api.keys.Keys;
import org.dockbox.hartshorn.api.keys.PersistentDataHolder;
import org.dockbox.hartshorn.api.keys.PersistentDataKey;
import org.dockbox.hartshorn.api.keys.TypedPersistentDataKey;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.item.ItemTypes;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.Getter;

public class Setting<T> extends TypedPersistentDataKey<T> {

    @Getter
    private final ResourceEntry resource;
    @Getter
    private final ResourceEntry description;

    private final Function<T, ResourceEntry> converter;
    private final Supplier<T> defaultValue;
    private final Supplier<Item> display;
    private final Consumer<PersistentDataHolder> action;

    public Setting(Class<T> type, String id, ResourceEntry name, ResourceEntry description, TypedOwner owner, Function<T, ResourceEntry> converter, Supplier<T> defaultValue,
                   Supplier<Item> display, Consumer<PersistentDataHolder> action) {
        super(name.plain(), id, owner, type);
        this.resource = name;
        this.description = description;
        this.converter = converter;
        this.defaultValue = defaultValue;
        this.display = display;
        this.action = action;
    }

    protected ResourceEntry convert(T value) {
        return this.converter.apply(value);
    }

    public Item item() {
        return this.display.get();
    }

    /**
     * Gets the setting value for the given data holder. If the setting isn't set for the given
     * holder, the default value is used. If the setting isn't set and the default value is absent,
     * {@code null} is returned.
     *
     * @param holder The data holder to test the setting on
     * @return The set value, default value, or null
     */
    @Nullable
    public T get(PersistentDataHolder holder) {
        return holder.get(this).orElse(this.defaultValue::get).orNull();
    }

    public static <T> SettingBuilder<T> of(Class<T> type) {
        return new SettingBuilder<>(type);
    }

    public Consumer<PersistentDataHolder> action() {
        return this.action;
    }

    public static final class SettingBuilder<T> {

        private String id;
        private TypedOwner owner;
        private Class<T> type;
        private ResourceEntry resource;
        private ResourceEntry description;
        private Function<T, ResourceEntry> converter = o -> new FakeResource(String.valueOf(o));
        private Supplier<T> defaultValue;
        private Supplier<Item> display = () -> Item.of(ItemTypes.BARRIER);
        private Consumer<PersistentDataHolder> action = holder -> {};

        private SettingBuilder(Class<T> type) {
            this.type = type;
        }

        public SettingBuilder<T> id(String id) {
            this.id = id;
            return this;
        }

        public SettingBuilder<T> owner(TypedOwner owner) {
            this.owner = owner;
            return this;
        }

        public SettingBuilder<T> owner(Class<?> owner) {
            this.owner = Hartshorn.context().meta().lookup(owner);
            return this;
        }

        public SettingBuilder<T> type(Class<T> type) {
            this.type = type;
            return this;
        }

        public SettingBuilder<T> resource(ResourceEntry resource) {
            this.resource = resource;
            return this;
        }

        public SettingBuilder<T> description(ResourceEntry description) {
            this.description = description;
            return this;
        }

        public SettingBuilder<T> converter(Function<T, ResourceEntry> converter) {
            this.converter = converter;
            return this;
        }

        public SettingBuilder<T> defaultValue(Supplier<T> defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public SettingBuilder<T> display(Supplier<Item> display) {
            this.display = display;
            return this;
        }

        public SettingBuilder<T> from(PersistentDataKey<T> key) {
            return this.id(key.id()).type(key.type());
        }

        public SettingBuilder<T> action(Consumer<PersistentDataHolder> action) {
            this.action = action;
            return this;
        }

        public Setting<T> ok() {
            if (this.type == null) throw new IllegalArgumentException("Type should be specified");
            if (this.owner == null) throw new IllegalArgumentException("Owned should be specified");
            if (this.resource == null) throw new IllegalArgumentException("Resource should be specified");
            if (this.description == null) throw new IllegalArgumentException("Description should be specified");
            if (this.defaultValue == null) throw new IllegalArgumentException("Default value should be specified");
            if (this.id == null) {
                this.id = Keys.id(this.resource.plain(), this.owner);
            }

            return new Setting<>(
                    this.type,
                    this.id,
                    this.resource,
                    this.description,
                    this.owner,
                    this.converter,
                    this.defaultValue,
                    this.display,
                    this.action);
        }
    }
}
