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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.web.geo.GeoLocation;
import org.terasology.web.geo.GeoLocationService;
import org.terasology.web.geo.dbip.GeoLocationServiceDbIp;

import com.google.common.collect.Lists;

/**
 * @author Martin Steiger
 */
public class ServerTable {

    private static final Logger logger = LoggerFactory.getLogger(ServerTable.class);

    /**
     * Retrieves the contents of a table
     * @param dataSource the database connection data source
     * @param tableName the name of the table in the DB
     * @return a list of rows
     * @throws SQLException if the table query fails
     * @throws IOException if the connection fails
     */
    public static List<Map<String,Object>> readAll(DataSource dataSource, String tableName) throws SQLException, IOException {

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

    public static void insert(DataSource dataSource, String tableName, String name, String address, int port) throws SQLException {
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
                    String template = "INSERT INTO %s (name, address, port, country, stateprov, city) values('%s', '%s', %d, '%s', '%s', '%s');";
                    String country = geoLoc.getCountry();
                    String stateProv = geoLoc.getStateOrProvince();
                    String city = geoLoc.getCity();
                    insert = String.format(template, escTableName, name, address, port, country, stateProv, city);

                } catch (IOException e) {
                    logger.error("Could not resolve {}", address, e);

                    String template = "INSERT INTO %s (name, address, port) values('%s', '%s', %d);";
                    insert = String.format(template, escTableName, name, address, port);
                }

                int affected = stmt.executeUpdate(insert);

                logger.info("Complete - {} rows affected", affected);
            }
        }
    }

    public static void remove(DataSource dataSource, String tableName, String address, int port) throws SQLException {
        String escTableName = "\"" + tableName + "\"";

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String template = "DELETE FROM %s WHERE address='%s' AND port=%d;";
                String cmd = String.format(template, escTableName, address, port);

                int affected = stmt.executeUpdate(cmd);

                logger.info("Complete - {} rows affected", affected);
            }
        }
    }
}
