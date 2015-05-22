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
import java.time.LocalDateTime;
import java.util.Collections;

import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.mvc.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.version.Version;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * @author Martin Steiger
 */
@Path("/")
public class Serveletty {

    private static final Logger logger = LoggerFactory.getLogger(Serveletty.class);

    private final DataSource dataSource;

    private final String tableName;

    public Serveletty(DataSource dataSource, String tableName) {
        Preconditions.checkArgument(dataSource != null, "dataSource must not be null");
        Preconditions.checkArgument(tableName != null, "tableName must not be null");

        this.dataSource = dataSource;
        this.tableName = tableName;
    }

    @GET
    @Path("show")
    @Produces(MediaType.TEXT_HTML)
    public Viewable show() {
        logger.info("Requested server list as HTML");
        ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                .put("items", list())
                .put("tab", "show")
                .put("year", LocalDateTime.now().getYear())
                .put("version", Version.getVersion())
                .build();
        return new Viewable("/server-list.ftl", dataModel);
    }

    @GET
    @Path("about")
    @Produces(MediaType.TEXT_HTML)
    public Viewable about() {
        logger.info("Requested about as HTML");
        ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                .put("tab", "about")
                .put("year", LocalDateTime.now().getYear())
                .put("version", Version.getVersion())
                .build();
        return new Viewable("/about.ftl", dataModel);
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

//    @GET
//    @Path("add")
//    @Produces(MediaType.APPLICATION_JSON)
    public Response add(@QueryParam("name") String name, @QueryParam("address") String address, @QueryParam("port") int port,
            @QueryParam("owner") String owner) {

        logger.info("Requested addition: name: {}, address: {}, port:{}", name, address, port);

        try {
            if (name == null) {
                return Response.fail("No name specified");
            }

            if (name.length() < 3 || name.length() > 20) {
                return Response.fail("Name length must be in [3..20]");
            }

            if (port < 1024 || port > 65535) {
                return Response.fail("Port must be in [1024..65535]");
            }

            if (address == null) {
                return Response.fail("No address specified");
            }

            if (owner == null || owner.isEmpty()) {
                return Response.fail("No owner specified");
            }

            InetAddress byName = InetAddress.getByName(address);
            if (!byName.isReachable(5000)) {
                return Response.fail("Unreachable host: " + address + " (" + byName.getHostAddress() + ")");
            }

            ServerTable.insert(dataSource, tableName, name, address, port, owner);

        } catch (UnknownHostException e) {
            logger.error("Could not resolve host: " + e.getMessage());
            return Response.fail("Unknown host: " + address);
        } catch (IOException e) {
            logger.error("Could not connect: ", e);
            return Response.fail("Could not connect to database");
        } catch (SQLException e) {
            logger.error("Could not query server table: " + e.getMessage());
            return Response.fail(e.getMessage());
        }
        return Response.success("Entry added");
    }

//    @GET
//    @Path("remove")
//    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@QueryParam("address") String address, @QueryParam("port") int port) {

        if (address == null) {
            return Response.fail("No address specified");
        }

        if (port == 0) {
            return Response.fail("No port specified");
        }

        try {
            if (ServerTable.remove(dataSource, tableName, address, port)) {
                return Response.success("Entry removed");
            } else {
                return Response.fail("Entry not found");
            }
        } catch (SQLException e) {
            return Response.fail(e.getMessage());
        }
    }
}
