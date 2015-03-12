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

import java.io.IOException;
import java.net.URI;

import javax.sql.PooledConnection;

import org.postgresql.ds.PGConnectionPoolDataSource;

/**
 * @author Martin Steiger
 */
public class Database {

    public static PooledConnection getPooledConnection() throws IOException {
        try {
            String env = System.getenv("DATABASE_URL");
            URI dbUri = new URI(env);
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            int port = dbUri.getPort();

            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + port + dbUri.getPath() + "?sslmode=require";

            PGConnectionPoolDataSource ds = new PGConnectionPoolDataSource();
            ds.setUrl(dbUrl);
            ds.setUser(username);
            ds.setPassword(password);

            PooledConnection pool = ds.getPooledConnection();
            return pool;
        }
        catch (Exception e) {
            throw new IOException(e);
        }

    }
}
