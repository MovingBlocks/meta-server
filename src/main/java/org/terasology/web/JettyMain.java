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

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.web.artifactory.ArtifactoryRepo;
import org.terasology.web.db.DataBase;
import org.terasology.web.db.JooqDatabase;
import org.terasology.web.geo.GeoLocationService;
import org.terasology.web.geo.dbip.GeoLocationServiceDbIp;
import org.terasology.web.io.GsonMessageBodyHandler;
import org.terasology.web.model.ModuleListModelImpl;
import org.terasology.web.model.ServerListModel;
import org.terasology.web.model.ServerListModelImpl;
import org.terasology.web.servlet.AboutServlet;
import org.terasology.web.servlet.ModuleServlet;
import org.terasology.web.servlet.ServerServlet;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


/**
 * @author Martin Steiger
 */
public final class JettyMain {

    private static final Logger logger = LoggerFactory.getLogger(JettyMain.class);

    private JettyMain() {
        // no instances
    }

    /**
     * @param args ignored
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        String portEnv = System.getenv("PORT");
        if (portEnv == null) {
            portEnv = "8080";
            logger.warn("Environment variable 'PORT' not defined - using default {}", portEnv);
        }
        Integer port = Integer.valueOf(portEnv);

        String dbEnv = System.getenv("DATABASE_URL");
        if (dbEnv == null) {
            logger.error("Environment variable 'DATABASE_URL' not defined!");
            return;
        }
        URI dbUri = new URI(dbEnv);

        String secret = System.getenv("EDIT_SECRET");
        if (secret == null) {
            logger.error("Environment variable 'EDIT_SECRET' not defined!");
            return;
        }

        String dbIpApiKey = System.getenv("DBIP_API_KEY");
        if (dbIpApiKey == null) {
            logger.warn("Environment variable 'DBIP_API_KEY' not defined - geo location lookup not available");
        }

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        int dbPort = dbUri.getPort();

        String host = "http://artifactory.terasology.org/artifactory";
        String releaseRepo = "terasology-release-local";
        String snapshotRepo = "terasology-snapshot-local";

        Path cacheFolder = Paths.get("cache", "modules");

        Path releaseRepoCacheFolder = cacheFolder.resolve(releaseRepo);
        Path snapshotRepoCacheFolder = cacheFolder.resolve(snapshotRepo);
        ArtifactoryRepo releaseRepository = ArtifactoryRepo.release(host, releaseRepo, releaseRepoCacheFolder);
        ArtifactoryRepo snapshotRepository = ArtifactoryRepo.snapshot(host, snapshotRepo, snapshotRepoCacheFolder);

        ModuleListModelImpl moduleListModel = new ModuleListModelImpl(cacheFolder);
        moduleListModel.addRepository(releaseRepository);
        moduleListModel.addRepository(snapshotRepository);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://" + dbUri.getHost() + ":" + dbPort + dbUri.getPath());
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("sslmode", "require");
        config.setMaximumPoolSize(3);
        config.setMinimumIdle(1);

        GeoLocationService geoService = new GeoLocationServiceDbIp(dbIpApiKey);

        // this is mostly for I18nMap, but can have an influence on other
        // string formats. Note that metainfo.ftl explicitly sets the locale to
        // define the date format.
        Locale.setDefault(Locale.ENGLISH);

        try (HikariDataSource ds = new HikariDataSource(config)) {

            DataBase dataBase = new JooqDatabase(ds, geoService);
            ServerListModel serverListModel = new ServerListModelImpl(dataBase, "servers", secret);

            Server server = createServer(port.intValue(),
                    new AboutServlet(),
                    new ServerServlet(serverListModel),          // the server list servlet
                    new ModuleServlet(moduleListModel));         // the module list servlet

            server.start();
            logger.info("Server started on port {}!", port);

            new Thread(moduleListModel::updateAllModules).start();

            server.join();
        }
    }

    public static Server createServer(int port, Object... servlets) throws Exception {
        Server server = new Server(port);

        ResourceHandler logFileResourceHandler = new ResourceHandler();
        logFileResourceHandler.setDirectoriesListed(true);
        logFileResourceHandler.setResourceBase("logs");

        ContextHandler logContext = new ContextHandler("/logs"); // the server uri path
        logContext.setHandler(logFileResourceHandler);

        ResourceHandler webResourceHandler = new ResourceHandler();
        webResourceHandler.setDirectoriesListed(false);
        webResourceHandler.setResourceBase("web");

        ContextHandler webContext = new ContextHandler("/");     // the server uri path
        webContext.setHandler(webResourceHandler);

        ResourceConfig rc = new ResourceConfig();
        rc.register(new GsonMessageBodyHandler());               // register JSON serializer
        rc.register(FreemarkerMvcFeature.class);

        for (Object servlet : servlets) {
            rc.register(servlet);
        }

        ServletContextHandler jerseyContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        jerseyContext.setContextPath("/");
        jerseyContext.setResourceBase("templates");
        jerseyContext.addServlet(new ServletHolder(new ServletContainer(rc)), "/*");

        HandlerList handlers = new HandlerList();
        handlers.addHandler(logContext);
        handlers.addHandler(webContext);
        handlers.addHandler(jerseyContext);

        server.setHandler(handlers);

        return server;
    }
}
