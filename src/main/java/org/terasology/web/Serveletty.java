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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Objects;

import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.mvc.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

/**
 * @author Martin Steiger
 */
@Path("/")
public class Serveletty {

    private static final Logger logger = LoggerFactory.getLogger(Serveletty.class);

    private final DataSource dataSource;

    private final String tableName;

    private final String editSecret;

    public Serveletty(DataSource dataSource, String tableName, String editSecret) {
        this.dataSource = dataSource;
        this.tableName = tableName;
        this.editSecret = editSecret;
    }

    @GET
    @Path("show")
    @Produces(MediaType.TEXT_HTML)
    public Viewable show() {
        logger.info("Requested server list as HTML");
        return new Viewable("/index.ftl", ImmutableMap.of("items", list()));
    }


    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Object list() {
        logger.info("Requested server list");
        try {
            return ServerTable.readAll(dataSource, tableName);
        } catch (SQLException e) {
            logger.error("Could not query server table: " + e.getMessage());
            return Collections.emptyList();
        } catch (IOException e) {
            logger.error("Could not connect to database", e);
            return Collections.emptyList();
        }
    }

    @GET
    @Path("add")
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(@QueryParam("name") String name, @QueryParam("address") String address, @QueryParam("port") int port, @QueryParam("secret") String secret) {

        logger.info("Requested addition: name: {}, address: {}, port:{}", name, address, port);

        try {
            if (name == null) {
                return new Response(false, "No name specified");
            }
            if (name.length() < 3 || name.length() > 20) {
                return new Response(false, "Name length must be in [3..20]");
            }
            if (port < 1024 || port > 65535) {
                return new Response(false, "Port must be in [1024..65535]");
            }
            if (address == null) {
                return new Response(false, "No address specified");
            }
            InetAddress byName = InetAddress.getByName(address);
            if (!byName.isReachable(5000)) {
                return new Response(false, "Unreachable host: " + address + " (" + byName.getHostAddress() + ")");
            }

            if (!Objects.equals(editSecret, secret)) {
                return new Response(false, "Invalid secret key");
            }

            ServerTable.insert(dataSource, tableName, name, address, port);

        } catch (UnknownHostException e) {
            logger.error("Could not resolve host: " + e.getMessage());
            return new Response(false, "Unknown host: " + address);
        } catch (IOException e) {
            logger.error("Could not connect: ", e);
            return new Response(false, "Could not connect to database");
        } catch (SQLException e) {
            logger.error("Could not query server table: " + e.getMessage());
            return new Response(false, e.getMessage());
        }
        return new Response(true, "Entry added");
    }

    @GET
    @Path("remove")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@QueryParam("address") String address, @QueryParam("port") int port, @QueryParam("secret") String secret) {

        if (address == null) {
            return new Response(false, "No address specified");
        }

        if (port == 0) {
            return new Response(false, "No port specified");
        }

        if (!Objects.equals(editSecret, secret)) {
            return new Response(false, "Invalid secret key");
        }

        try {
            if (ServerTable.remove(dataSource, tableName, address, port)) {
                return new Response(true, "Entry removed");
            } else {
                return new Response(false, "Entry not found");
            }
        } catch (SQLException e) {
            return new Response(false, e.getMessage());
        }
    }
}
