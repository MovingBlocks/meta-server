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

package org.terasology.master;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;


/**
 *
 * @author Martin Steiger
 */
public class Maintenance {

    public static void main(String[] args) throws SQLException, URISyntaxException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));
        DataSource source = Database.getPooledConnection(dbUri);

        try (Connection conn = source.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
//                stmt.execute("DROP TABLE servers");

                String createTable = "CREATE TABLE IF NOT EXISTS servers ("
                        + "id      SERIAL PRIMARY KEY,"
                        + "name    varchar(128),"
                        + "address varchar(128),"
                        + "port    integer,"
                        + "modtime timestamp DEFAULT current_timestamp"
                        + ");";
                stmt.execute(createTable);

                String insert = String.format("INSERT INTO servers (name, address, port) values('%s', '%s', %d);",
                        "myServerName", "myHostAddress", 25777);
                stmt.executeUpdate(insert);
            }
        }
    }
}
