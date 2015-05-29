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

import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.terasology.web.DataBase;
import org.terasology.web.JettyMain;
import org.terasology.web.JooqDatabase;
import org.terasology.web.model.ServerEntry;
import org.terasology.web.model.ServerListModel;
import org.terasology.web.model.ServerListModelImpl;

public abstract class WebServerBasedTests {

    protected static final int PORT = 8082;
    protected static final String URL_BASE = "http://localhost:" + PORT;
    // Keep the content of an in-memory database as long as the virtual machine is alive
    protected static final String DB_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    protected static final String SERVER_TABLE = "servers";
    protected static ServerEntry firstEntry;

    private static DataBase dataBase;
    private static Server server;

    @BeforeClass
    public static void setup() throws Exception {

        String secret = "edit";

        dataBase = new JooqDatabase(DB_URL);

        ServerListModel serverListModel = new ServerListModelImpl(dataBase, SERVER_TABLE, secret);

        server = JettyMain.start(PORT, serverListModel);

        dataBase.createTable(SERVER_TABLE);

        firstEntry = new ServerEntry("localhost", 25000);
        firstEntry.setName("myName");
        firstEntry.setOwner("Tester");
        dataBase.insert(SERVER_TABLE, firstEntry);
    }

    @AfterClass
    public static void shutdown() throws Exception {
        server.stop();
    }
}
