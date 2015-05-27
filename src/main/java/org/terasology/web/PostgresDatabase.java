/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.web;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.web.geo.GeoLocation;
import org.terasology.web.geo.GeoLocationService;
import org.terasology.web.geo.dbip.GeoLocationServiceDbIp;

import com.google.common.collect.Lists;

/**
 * @author Martin Steiger
 */
public final class PostgresDatabase implements DataBase {

    private static final Logger logger = LoggerFactory.getLogger(PostgresDatabase.class);

    private final PGSimpleDataSource dataSource;

    /**
     * @param dbUri the database URI in the form <code>postgres://user:pass@url:port/database</code>.
     */
    public PostgresDatabase(URI dbUri) {

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        int port = dbUri.getPort();

        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + port + dbUri.getPath();

        dataSource = new PGSimpleDataSource();
        dataSource.setReadOnly(true);
        dataSource.setUrl(dbUrl);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setSslMode("require");
    }

    @Override
    public boolean remove(String tableName, String address, int port) throws SQLException {
        String escTableName = "\"" + tableName + "\"";

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String template = "DELETE FROM %s WHERE address='%s' AND port=%d;";
                String cmd = String.format(template, escTableName, address, port);

                int affected = stmt.executeUpdate(cmd);

                // If everything went well, exactly 1 row should have been affected (=removed)
                return (affected == 1);
            }
        }
    }

    @Override
    public List<Map<String, Object>> readAll(String tableName) throws SQLException, IOException {

        try (Connection connection = dataSource.getConnection()) {
            try (Statement stmt = connection.createStatement()) {

                List<Map<String, Object>> entries = Lists.newArrayList();

                try (ResultSet rs = stmt.executeQuery("SELECT * FROM \"" + tableName + "\"")) {

                    ResultSetMetaData metaData = rs.getMetaData();
                    while (rs.next()) {
                        Map<String, Object> entry = new LinkedHashMap<>();

                        for (int i = 1; i <= metaData.getColumnCount(); i++) {
                            entry.put(metaData.getColumnLabel(i), rs.getObject(i));
                        }
                        entries.add(entry);
                    }
                }

                return entries;
            }
        }
    }

    @Override
    public boolean insert(String tableName, String name, String address, int port, String owner) throws SQLException {
        String escTableName = "\"" + tableName + "\"";

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {

                String createTable = "CREATE TABLE IF NOT EXISTS " + escTableName + " ("
                        + "name      varchar(256),"
                        + "address   varchar(256),"
                        + "port      integer,"
                        + "country   varchar(256),"
                        + "stateprov varchar(256),"
                        + "city      varchar(256),"
                        + "owner     varchar(256),"
                        + "modtime   timestamp DEFAULT current_timestamp,"
                        + "PRIMARY KEY (address, port)"
                        + ");";
                stmt.execute(createTable);
                if (stmt.getWarnings() != null) {
                    logger.info(stmt.getWarnings().toString());
                }

                String insert;

                GeoLocationService geoService = new GeoLocationServiceDbIp();
                try {
                    GeoLocation geoLoc = geoService.resolve(address);
                    String country = geoLoc.getCountry();
                    String stateProv = geoLoc.getStateOrProvince();
                    String city = geoLoc.getCity();
                    insert = String.format("INSERT INTO %s "
                            + "(name, address, port, country, stateprov, city, owner) "
                            + "values('%s', '%s', %d, '%s', '%s', '%s', '%s');",
                            escTableName,
                            name, address, port, country, stateProv, city, owner);

                } catch (IOException e) {
                    logger.error("Could not resolve geo-location for {}", address, e);

                    String template = "INSERT INTO %s (name, address, port, owner) values('%s', '%s', %d, '%s');";
                    insert = String.format(template, escTableName, name, address, port, owner);
                }

                int affected = stmt.executeUpdate(insert);
                logger.info("Complete - {} rows affected", affected);
                return (affected == 1);
            }
        }
    }

    @Override
    public boolean update(String tableName, String name, String address, int port, String owner) throws SQLException {
        String escTableName = "\"" + tableName + "\"";

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {

                String update;
                GeoLocationService geoService = new GeoLocationServiceDbIp();
                try {
                    GeoLocation geoLoc = geoService.resolve(address);
                    String country = geoLoc.getCountry();
                    String stateProv = geoLoc.getStateOrProvince();
                    String city = geoLoc.getCity();
                    update = String.format("UPDATE %s "
                            + "SET name = '%s', country = '%s', stateprov = '%s', city = '%s', owner = '%s' "
                            + "WHERE address = '%s' AND port = '%d';",
                            escTableName,
                            name, country, stateProv, city, owner, address, port);

                } catch (IOException e) {
                    logger.error("Could not resolve geo-location for {}", address, e);

                    update = String.format("UPDATE %s "
                            + "SET name = '%s', owner = '%s' "
                            + "WHERE address = '%s' AND port = '%d';",
                            escTableName,
                            name, owner, address, port);
                }

                int affected = stmt.executeUpdate(update);
                return (affected == 1);
            }
        }
    }

}
