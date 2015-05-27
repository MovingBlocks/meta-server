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
import java.sql.SQLException;

import org.terasology.web.DataBase;
import org.terasology.web.PostgresDatabase;


/**
 *
 * @author Martin Steiger
 */
public class Maintenance {

    public static void main(String[] args) throws SQLException, URISyntaxException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));
        DataBase db = new PostgresDatabase(dbUri);

        String tableName = "servers";

        String name = "tuffkidtek";
        String address = "terasology.tuffkidtek.com";
        int port = 25777;

        db.insert(tableName, name, address, port, "minnesotags");
    }
}

