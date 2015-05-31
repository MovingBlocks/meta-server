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

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.terasology.web.JettyMain;
import org.terasology.web.db.DataBase;
import org.terasology.web.db.JooqDatabase;
import org.terasology.web.model.ModuleListModel;
import org.terasology.web.model.ModuleListModelImpl;
import org.terasology.web.model.ServerEntry;
import org.terasology.web.model.ServerListModel;
import org.terasology.web.model.ServerListModelImpl;

public abstract class WebServerBasedTests {

    protected static final int PORT = 8082;
    protected static final String URL_BASE = "http://localhost:" + PORT;
    protected static final String SERVER_TABLE = "servers";
    protected static ServerEntry firstEntry;

    private static DataBase dataBase;
    private static Server webServer;
    private static Connection dummyConn;
    private static AtomicInteger atomCount = new AtomicInteger();

    @BeforeClass
    public static void setup() throws Exception {

        String secret = "edit";

        // make a unique database for each testing class
        String dbUri = "jdbc:h2:mem:test_" + atomCount.getAndIncrement();

        // Open a dummy connection to the in-memory database to keep it alive
        dummyConn = DriverManager.getConnection(dbUri);
        dataBase = new JooqDatabase(dbUri);

        ServerListModel serverListModel = new ServerListModelImpl(dataBase, SERVER_TABLE, secret);
        ModuleListModel moduleListModel = new ModuleListModelImpl();

        webServer = JettyMain.start(PORT, serverListModel, moduleListModel);

        dataBase.createTable(SERVER_TABLE);

        firstEntry = new ServerEntry("localhost", 25000);
        firstEntry.setName("myName");
        firstEntry.setOwner("Tester");
        dataBase.insert(SERVER_TABLE, firstEntry);
    }

    @AfterClass
    public static void shutdown() throws Exception {
        webServer.stop();
        dummyConn.close();
    }
}
