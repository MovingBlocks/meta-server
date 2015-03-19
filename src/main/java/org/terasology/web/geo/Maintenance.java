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

package org.terasology.web.geo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.web.Database;
import org.terasology.web.geo.dbip.GeoLocationServiceDbIp;


/**
 *
 * @author Martin Steiger
 */
public class Maintenance {

    private static final Logger logger = LoggerFactory.getLogger(Maintenance.class);

    public static void main(String[] args) throws SQLException, URISyntaxException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));
        DataSource source = Database.getDatabaseConnection(dbUri);

        String tableName = "servers";
        String escTableName = "\"" + tableName + "\"";

        try (Connection conn = source.getConnection()) {
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

                String name = "tuffkidtek";
                String address = "terasology.tuffkidtek.com";
                int port = 25777;

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
}
