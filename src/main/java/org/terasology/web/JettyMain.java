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
import java.util.Locale;
import java.util.Properties;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.postgresql.PGProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.web.db.DataBase;
import org.terasology.web.db.JooqDatabase;
import org.terasology.web.io.GsonMessageBodyHandler;
import org.terasology.web.model.ModuleListModel;
import org.terasology.web.model.ModuleListModelImpl;
import org.terasology.web.model.ServerListModel;
import org.terasology.web.model.ServerListModelImpl;
import org.terasology.web.servlet.AboutServlet;
import org.terasology.web.servlet.ModuleServlet;
import org.terasology.web.servlet.ServerServlet;


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

        Integer port = Integer.valueOf(System.getenv("PORT"));
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        Locale.setDefault(Locale.ENGLISH);

        String secret = System.getenv("EDIT_SECRET");

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        int dbPort = dbUri.getPort();

        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbPort + dbUri.getPath();

        Properties props = new Properties();
        props.setProperty(PGProperty.USER.getName(), username);
        props.setProperty(PGProperty.PASSWORD.getName(), password);
        props.setProperty(PGProperty.SSL_MODE.getName(), "require");
        DataBase dataBase = new JooqDatabase(dbUrl, props);

        ServerListModel serverListModel = new ServerListModelImpl(dataBase, "servers", secret);
        ModuleListModel moduleListModel = new ModuleListModelImpl();

        Server server = start(port.intValue(), serverListModel, moduleListModel);
        server.join();
    }

    public static Server start(int port, ServerListModel serverListModel, ModuleListModel moduleListModel) throws Exception {
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
        rc.register(new AboutServlet());
        rc.register(new ServerServlet(serverListModel));         // register the server list servlet
        rc.register(new ModuleServlet(moduleListModel));         // register the module list servlet

        ServletContextHandler jerseyContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        jerseyContext.setContextPath("/");
        jerseyContext.setResourceBase("templates");
        jerseyContext.addServlet(new ServletHolder(new ServletContainer(rc)), "/*");

        HandlerList handlers = new HandlerList();
        handlers.addHandler(logContext);
        handlers.addHandler(webContext);
        handlers.addHandler(jerseyContext);

        server.setHandler(handlers);
        server.start();

        logger.info("Server started on port {}!", port);

        return server;
    }
}
