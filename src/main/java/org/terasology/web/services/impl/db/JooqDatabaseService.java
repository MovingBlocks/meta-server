// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.web.services.impl.db;

import jakarta.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertSetMoreStep;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SortField;
import org.jooq.Table;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.web.services.api.DatabaseService;
import org.terasology.web.services.api.GeoLocationService;
import org.terasology.web.services.impl.geo.GeoLocation;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Jooq Database service implementation.
 */
@Singleton
public final class JooqDatabaseService implements DatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(JooqDatabaseService.class);

    private final DataSource ds;

    private final GeoLocationService geoService;

    /**
     * @param ds         the datasource
     * @param geoService the geo-location service
     */
    public JooqDatabaseService(DataSource ds, GeoLocationService geoService) {
        this.geoService = geoService;
        this.ds = ds;
    }

    @Override
    public boolean remove(String tableName, String address, int port) throws SQLException {

        try (Connection conn = ds.getConnection()) {
            DSLContext context = DSL.using(conn);
            Table<Record> table = DSL.table(DSL.name(tableName));

            Field<Object> addressField = DSL.field(DSL.name("address"));
            Field<Object> portField = DSL.field(DSL.name("port"));

            // "DELETE FROM <tableName> WHERE address='<address>' AND port=<port>;"
            Query q = context.deleteFrom(table).where(addressField.eq(address).and(portField.eq(port)));
            int affected = q.execute();

            // If everything went well, exactly 1 row should have been affected (=removed)
            return (affected == 1);
        }
    }

    @Override
    public List<Map<String, Object>> readAll(String tableName) throws SQLException {

        try (Connection conn = ds.getConnection()) {
            DSLContext context = DSL.using(conn);
            Table<Record> table = DSL.table(DSL.name(tableName));

            SortField<?> activeDesc = DSL.field(DSL.name("active")).desc();
            SortField<?> modTimeDesc = DSL.field(DSL.name("modtime")).desc();
            Result<Record> content = context.select().from(table).orderBy(activeDesc, modTimeDesc).fetch();
            List<Map<String, Object>> entries = new ArrayList<>(content.size());

            for (Record record : content) {
                Map<String, Object> entry = new LinkedHashMap<>();

                for (int i = 0; i < record.size(); i++) {
                    entry.put(content.field(i).getName(), record.getValue(i));
                }
                entries.add(entry);
            }

            return entries;
        }
    }

    @Override
    public void createTable(String tableName) throws SQLException {
        try (Connection conn = ds.getConnection()) {
            DSLContext context = DSL.using(conn);
            Table<?> table = tableExists(context, tableName);
            if (table == null) {
                table = DSL.table(DSL.name(tableName));
                createTable(context, table);
            }
        }
    }

    private boolean insert(String tableName, Map<String, Object> data) throws SQLException {
        if (data.isEmpty()) {
            return true;
        }

        try (Connection conn = ds.getConnection()) {
            DSLContext context = DSL.using(conn);
            Table<?> table = DSL.table(DSL.name(tableName));

            Iterator<Entry<String, Object>> it = data.entrySet().iterator();
            Entry<String, Object> first = it.next();
            InsertSetMoreStep<?> statement = context.insertInto(table)
                    .set(DSL.field(DSL.name(first.getKey())), first.getValue());

            while (it.hasNext()) {
                Entry<String, Object> entry = it.next();
                statement.set(DSL.field(DSL.name(entry.getKey())), entry.getValue());
            }

            int affected = statement.execute();
            logger.info("Complete - {} rows affected", affected);
            return (affected == 1);
        }
    }

    @Override
    public boolean insert(String tableName, String name, String address, int port, String owner, boolean active) throws SQLException {

        try (Connection conn = ds.getConnection()) {
            DSLContext context = DSL.using(conn);
            Table<?> table = DSL.table(DSL.name(tableName));

            InsertSetMoreStep<?> statement = context.insertInto(table)
                    .set(DSL.field(DSL.name("name")), name)
                    .set(DSL.field(DSL.name("address")), address)
                    .set(DSL.field(DSL.name("port")), port)
                    .set(DSL.field(DSL.name("owner")), owner)
                    .set(DSL.field(DSL.name("active")), active);

            try {
                GeoLocation geoLoc = geoService.resolve(address);
                String country = geoLoc.getCountry();
                String stateProv = geoLoc.getStateOrProvince();
                String city = geoLoc.getCity();

                statement
                        .set(DSL.field(DSL.name("country")), country)
                        .set(DSL.field(DSL.name("stateprov")), stateProv)
                        .set(DSL.field(DSL.name("city")), city);

            } catch (IOException e) {
                logger.error("Could not resolve geo-location for {}", address, e);
            }

            int affected = statement.execute();
            logger.info("Complete - {} rows affected", affected);
            return (affected == 1);
        }
    }

    private void createTable(DSLContext context, Table<?> table) {

        context.createTable(table)
                .column("name", SQLDataType.VARCHAR.length(256))
                .column("address", SQLDataType.VARCHAR.length(256).nullable(false))
                .column("port", SQLDataType.INTEGER.nullable(false))
                .column("country", SQLDataType.VARCHAR.length(256))
                .column("stateprov", SQLDataType.VARCHAR.length(256))
                .column("city", SQLDataType.VARCHAR.length(256))
                .column("owner", SQLDataType.VARCHAR.length(256))
                .column("active", SQLDataType.BOOLEAN.nullable(false))
                .column("modtime", SQLDataType.TIMESTAMP)
                .execute();

        // set default value for active
        context.alterTable(table)
                .alter(DSL.field(DSL.name("active"), Boolean.class)).defaultValue(Boolean.FALSE)
                .execute();

        // modtime timestamp DEFAULT current_timestamp
        context.alterTable(table)
                .alter(DSL.field(DSL.name("modtime"), Timestamp.class)).defaultValue(DSL.currentTimestamp())
                .execute();

        // PRIMARY KEY (address, port)
        context.alterTable(table)
                .add(DSL.constraint("primary_key").primaryKey("address", "port"))
                .execute();
    }

    private Table<?> tableExists(DSLContext context, String tableName) {
        for (Table<?> tab : context.meta().getTables()) {
            if (tab.getName().equals(tableName)) {
                return tab;
            }
        }
        return null;
    }

    @Override
    public boolean update(String tableName, String name, String address, int port, String owner, boolean active) throws SQLException {

        try (Connection conn = ds.getConnection()) {
            DSLContext context = DSL.using(conn);
            Table<Record> table = DSL.table(DSL.name(tableName));

            UpdateSetMoreStep<Record> statement = context.update(table)
                    .set(DSL.field(DSL.name("name")), name)
                    .set(DSL.field(DSL.name("owner")), owner)
                    .set(DSL.field(DSL.name("active")), active)
                    .set(DSL.field(DSL.name("modtime")), DSL.defaultValue(Timestamp.class));

            try {
                GeoLocation geoLoc = geoService.resolve(address);
                String country = geoLoc.getCountry();
                String stateProv = geoLoc.getStateOrProvince();
                String city = geoLoc.getCity();

                statement
                        .set(DSL.field(DSL.name("country")), country)
                        .set(DSL.field(DSL.name("stateprov")), stateProv)
                        .set(DSL.field(DSL.name("city")), city);

            } catch (IOException e) {
                logger.error("Could not resolve geo-location for {}", address, e);
            }

            Field<Object> addressField = DSL.field(DSL.name("address"));
            Field<Object> portField = DSL.field(DSL.name("port"));

            int affected = statement
                    .where(addressField.eq(address).and(portField.eq(port)))
                    .execute();

            return (affected == 1);
        }
    }

}
