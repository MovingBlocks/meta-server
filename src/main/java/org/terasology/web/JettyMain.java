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

import javax.sql.DataSource;

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
import org.terasology.web.io.GsonMessageBodyHandler;


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

        Server server = new Server(port.intValue());
        DataSource dataSource = Database.getDatabaseConnection(dbUri);

        ResourceHandler logFileResourceHandler = new ResourceHandler();
        logFileResourceHandler.setDirectoriesListed(true);
        logFileResourceHandler.setResourceBase("logs");

        ContextHandler logContext = new ContextHandler("/logs"); // the server uri path
        logContext.setHandler(logFileResourceHandler);

        ResourceHandler webResourceHandler = new ResourceHandler();
        webResourceHandler.setDirectoriesListed(false);
        webResourceHandler.setResourceBase("web");

        ContextHandler webContext = new ContextHandler("/"); // the server uri path
        webContext.setHandler(webResourceHandler);

        ResourceConfig rc = new ResourceConfig();
        rc.register(new GsonMessageBodyHandler());               // register JSON serializer
        rc.register(FreemarkerMvcFeature.class);
        rc.register(new Serveletty(dataSource, "servers"));      // register the actual servlet

        ServletContextHandler jerseyContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        jerseyContext.setContextPath("/servers");
        jerseyContext.setResourceBase("templates");
        jerseyContext.addServlet(new ServletHolder(new ServletContainer(rc)), "/*");

        HandlerList handlers = new HandlerList();
        handlers.addHandler(logContext);
        handlers.addHandler(webContext);
        handlers.addHandler(jerseyContext);

        server.setHandler(handlers);
        server.start();

        logger.info("Server started on port {}!", port);

        server.join();
    }
}
