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

package org.dockbox.selene.sponge.objects.item.maps;

import org.dockbox.selene.core.PlayerStorageService;
import org.dockbox.selene.core.annotations.files.Format;
import org.dockbox.selene.core.files.FileManager;
import org.dockbox.selene.core.objects.Console;
import org.dockbox.selene.core.objects.Exceptional;
import org.dockbox.selene.core.objects.item.maps.CustomMap;
import org.dockbox.selene.core.objects.item.maps.CustomMapService;
import org.dockbox.selene.core.objects.targets.Identifiable;
import org.dockbox.selene.core.server.Selene;
import org.dockbox.selene.core.server.properties.AnnotationProperty;
import org.dockbox.selene.database.SQLMan;
import org.dockbox.selene.database.dialects.sqlite.SQLitePathProperty;
import org.dockbox.selene.database.exceptions.InvalidConnectionException;
import org.dockbox.selene.database.exceptions.NoSuchTableException;
import org.dockbox.selene.nms.maps.NMSMapUtils;
import org.dockbox.selene.structures.table.Table;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class SpongeCustomMapService implements CustomMapService
{

    private static final String TABLE = "uploaders";
    private static final int MAP_COLOR = 16744576;

    public SpongeCustomMapService()
    {
    }

    @Override
    public CustomMap create(BufferedImage image, Identifiable source)
    {
        int mapId = NMSMapUtils.populateColoredMap(image);
        return createCombinedMap(mapId, source);
    }

    @Override
    public CustomMap create(byte[] image, Identifiable source)
    {
        int mapId = NMSMapUtils.populateColoredMap(image);
        return createCombinedMap(mapId, source);
    }

    @Override
    public CustomMap getById(int id)
    {
        return createCombinedMap(id, lookupSource(id));
    }

    @Override
    public Collection<CustomMap> getFrom(Identifiable source)
    {
        return getHistoryTable().where(MapIdentifiers.SOURCE, source.getUniqueId())
                .getRows().stream()
                .map(row -> row.getValue(MapIdentifiers.MAP))
                .filter(Exceptional::isPresent)
                .map(Exceptional::get)
                .map(this::getById)
                .collect(Collectors.toCollection(ArrayList::new));
    }


    private static CustomMap createCombinedMap(int mapId, Identifiable source)
    {
        ItemStack stack = createItemStack(mapId);
        return new SpongeCustomMap(stack, source, mapId);
    }

    private static Identifiable lookupSource(int mapId)
    {
        return getHistoryTable().where(MapIdentifiers.MAP, mapId).first()
                .map(row -> row.getValue(MapIdentifiers.SOURCE).orElse(Console.UNIQUE_ID))
                .map(uniqueId -> Selene.provide(PlayerStorageService.class).getPlayer(uniqueId).orNull())
                .map(Identifiable.class::cast)
                .orElse(Console.getInstance());
    }


    private static ItemStack createItemStack(int mapId)
    {
        ItemStack stack = ItemStack.builder().itemType(ItemTypes.FILLED_MAP).quantity(1).build();
        DataView rawData = stack.toContainer();
        rawData.set(DataQuery.of("UnsafeDamage"), mapId);
        rawData.set(DataQuery.of("UnsafeData", "display", "LocName"), "item.painting.name");
        rawData.set(DataQuery.of("UnsafeData", "display", "MapColor"), MAP_COLOR);

        stack = ItemStack.builder().fromContainer(rawData).build();
        return stack;
    }

    private static void store()
    {
        try
        {
            SQLMan<?> sql = Selene.provide(SQLMan.class,
                    AnnotationProperty.of(Format.SQLite.class),
                    new SQLitePathProperty(getHistoryStorePath()));
            sql.store(TABLE, getHistoryTable());
        }
        catch (InvalidConnectionException e)
        {
            Selene.handle(e);
        }
    }

    private static Table getHistoryTable()
    {
        try
        {
            SQLMan<?> sql = Selene.provide(SQLMan.class,
                    AnnotationProperty.of(Format.SQLite.class),
                    new SQLitePathProperty(getHistoryStorePath()));
            return sql.getOrCreateTable(TABLE, SpongeCustomMapService.getEmptyTable());
        }
        catch (InvalidConnectionException | NoSuchTableException e)
        {
            Selene.handle(e);
            return SpongeCustomMapService.getEmptyTable();
        }
    }

    private static Path getHistoryStorePath() {
        FileManager fileManager = Selene.provide(FileManager.class);
        return fileManager.getDataFile(Selene.class, "maps.db");
    }

    private static Table getEmptyTable()
    {
        return new Table(MapIdentifiers.MAP, MapIdentifiers.SOURCE);
    }
}
