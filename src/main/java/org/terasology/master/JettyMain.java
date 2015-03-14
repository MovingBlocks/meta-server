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

import javax.sql.DataSource;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Steiger
 */
public class JettyMain {

    private static final Logger logger = LoggerFactory.getLogger(JettyMain.class);

    /**
     * @param args ignored
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        Integer port = Integer.valueOf(System.getenv("PORT"));
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        Server server = new Server(port.intValue());
        DataSource dataSource = Database.getPooledConnection(dbUri);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setResourceBase("logs");

        ContextHandler ctx = new ContextHandler("/logs"); // the server uri path
        ctx.setHandler(resourceHandler);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/servers");
        context.addServlet(new ServletHolder(new Serveletty(dataSource)), "/*");

        HandlerList handlers = new HandlerList();
        handlers.addHandler(ctx);
        handlers.addHandler(context);

        server.setHandler(handlers);
        server.start();

        logger.info("Server started on port {}!", port);

        server.join();
    }
}
