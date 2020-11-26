/*
 *  Copyright (C) 2020 Guus Lieben
 *
 *  This framework is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 2.1 of the
 *  License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 *  the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.selene.integrated.sql;

import org.dockbox.selene.core.objects.optional.Exceptional;
import org.dockbox.selene.core.objects.tuple.Tuple;
import org.dockbox.selene.core.server.properties.InjectableType;
import org.dockbox.selene.core.server.properties.InjectorProperty;
import org.dockbox.selene.core.util.SeleneUtils;
import org.dockbox.selene.integrated.data.table.Table;
import org.dockbox.selene.integrated.data.table.TableRow;
import org.dockbox.selene.integrated.data.table.column.ColumnIdentifier;
import org.dockbox.selene.integrated.data.table.column.SimpleColumnIdentifier;
import org.dockbox.selene.integrated.data.table.exceptions.IdentifierMismatchException;
import org.dockbox.selene.integrated.sql.exceptions.InvalidConnectionException;
import org.dockbox.selene.integrated.sql.properties.SQLColumnProperty;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SQLMan<T> implements InjectableType {

    private final Map<String, ColumnIdentifier<?>> identifiers = new HashMap<>();

    protected DSLContext getContext(T target) throws InvalidConnectionException {
        return DSL.using(this.getConnection(target), this.getDialect());
    }

    protected abstract SQLDialect getDialect();

    protected abstract Connection getConnection(T target) throws InvalidConnectionException;

    protected abstract T getDefaultTarget();

    public Table getTable(String name) throws InvalidConnectionException {
        return this.getTable(name, this.getDefaultTarget());
    }

    public Table getTable(String name, T target) throws InvalidConnectionException {
        Result<Record> results = this.getContext(target).select().from(name).fetch();
        return this.convertToTable(results);
    }

    public void store(String name, Table table) {}

    public void store(Path filePath, String name, Table table) {}

    private Table convertToTable(Result<Record> results) {
        if (results.isEmpty()) return new Table();

        Table table = new Table(this.getIdentifiers(results));
        results.map((Record record) -> this.convertToTableRow(record, table))
                .forEach(row -> {
                    try {
                        table.addRow(row);
                    } catch (IdentifierMismatchException e) {
                        throw new IllegalStateException("Loaded identifiers did not match while populating table", e);
                    }
                });

        return table;
    }

    private List<ColumnIdentifier<?>> getIdentifiers(Result<Record> results) {
        List<ColumnIdentifier<?>> identifiers = new ArrayList<>();
        for (Field<?> field : results.get(0).fields()) {
            String name = field.getName();

            ColumnIdentifier<?> identifier = this.tryGetColumn(name).orElseGet(() -> {
                Class<?> type = field.getDataType().getType();
                return new SimpleColumnIdentifier<>(name, type);
            });
            identifiers.add(identifier);
        }
        return identifiers;
    }

    private TableRow convertToTableRow(Record record, Table table) {
        TableRow row = new TableRow();
        for (Field<?> field : record.fields()) {
            Object attr = record.getValue(field);
            ColumnIdentifier<?> identifier = table.getIdentifier(field.getName());
            if (null != identifier)
                row.addValue(identifier, attr);
        }
        return row;
    }

    private Exceptional<ColumnIdentifier<?>> tryGetColumn(String key) {
        return Exceptional.ofNullable(this.identifiers.get(key));
    }

    @Override
    public void stateEnabling(InjectorProperty<?>... properties) {
        SeleneUtils.getSubProperties(SQLColumnProperty.class, properties)
                .forEach(property -> {
                    Tuple<String, ColumnIdentifier<?>> identifier = property.getObject();
                    this.identifiers.put(identifier.getKey(), identifier.getValue());
                });
    }

    @Override
    public boolean canEnable() {
        return true;
    }
}
