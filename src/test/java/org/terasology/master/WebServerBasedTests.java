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
import org.h2.jdbcx.JdbcDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import org.terasology.web.JettyMain;
import org.terasology.web.artifactory.ArtifactRepository.RepoType;
import org.terasology.web.db.DataBase;
import org.terasology.web.db.JooqDatabase;
import org.terasology.web.geo.GeoLocation;
import org.terasology.web.geo.GeoLocationService;
import org.terasology.web.model.ModuleListModelImpl;
import org.terasology.web.model.ServerEntry;
import org.terasology.web.model.ServerListModel;
import org.terasology.web.model.ServerListModelImpl;
import org.terasology.web.servlet.AboutController;
import org.terasology.web.servlet.ServerServlet;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class WebServerBasedTests {

    @ClassRule
    public static TemporaryFolder tempFolder = new TemporaryFolder();

    protected static final int PORT = 8082;
    protected static final String URL_BASE = "http://localhost:" + PORT;
    protected static final String SERVER_TABLE = "servers";
    protected static ServerEntry firstEntry;

    private static DataBase dataBase;
    private static Server webServer;
    private static Connection dummyConn;
    private static AtomicInteger atomCount = new AtomicInteger();

    private static DummyArtifactRepo releaseRepo;
    private static DummyArtifactRepo snapshotRepo;

    @BeforeClass
    public static void setup() throws Exception {

        String secret = "edit";

        // make a unique database for each testing class
        String dbUri = "jdbc:h2:mem:test_" + atomCount.getAndIncrement();

        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(dbUri);

        GeoLocationService geoService = new DummyGeoLocationService();

        // Open a dummy connection to the in-memory database to keep it alive
        dummyConn = DriverManager.getConnection(dbUri);
        dataBase = new JooqDatabase(ds, geoService);

        File cacheFolder = tempFolder.newFolder("module", "cache");
        ModuleListModelImpl moduleListModel = new ModuleListModelImpl(cacheFolder.toPath(), new DummyExtractor());

        releaseRepo = new DummyArtifactRepo(RepoType.RELEASE);
        addRelease("Core", "Core-0.53.1.jar_info.json");

        snapshotRepo = new DummyArtifactRepo(RepoType.SNAPSHOT);
        addSnapshot("ChrisVolume1OST", "ChrisVolume1OST-0.2.1-20150608.034649-1.jar_info.json");
        addSnapshot("MusicDirector", "MusicDirector-0.2.1-20150608.041945-1.jar_info.json");

        moduleListModel.addRepository(releaseRepo);
        moduleListModel.addRepository(snapshotRepo);

        moduleListModel.updateAllModules();

        ServerListModel serverListModel = new ServerListModelImpl(dataBase, SERVER_TABLE, secret);

        webServer = JettyMain.createServer(PORT,
                new AboutController(),
                new ServerServlet(serverListModel));         // the server list servlet
//                new ModuleServlet(moduleListModel));         // the module list servlet

        webServer.start();

        dataBase.createTable(SERVER_TABLE);

        GeoLocation geo = geoService.resolve("localhost");
        firstEntry = new ServerEntry("localhost", 25000);
        firstEntry.setName("myName");
        firstEntry.setOwner("Tester");
        firstEntry.setCountry(geo.getCountry());
        firstEntry.setStateprov(geo.getStateOrProvince());
        firstEntry.setCity(geo.getCity());
        dataBase.insert(SERVER_TABLE, firstEntry);
    }

    protected static void addRelease(String modName, String fname) throws IOException {
        releaseRepo.addArtifact(modName, new ClasspathArtifactInfo("/metas/" + fname));
    }

    protected static void addSnapshot(String modName, String fname) throws IOException {
        snapshotRepo.addArtifact(modName, new ClasspathArtifactInfo("/metas/" + fname));
    }

    @AfterClass
    public static void shutdown() throws Exception {
        webServer.stop();
        dummyConn.close();
    }
}
