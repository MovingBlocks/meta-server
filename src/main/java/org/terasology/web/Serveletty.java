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

import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Steiger
 */
@Path("/")
public class Serveletty {

    private static final Logger logger = LoggerFactory.getLogger(Serveletty.class);

    private final DataSource dataSource;

    public Serveletty(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Object list() {
        logger.info("Requested server list");
        try {
            return ServerTable.readAll(dataSource, "servers");
        } catch (IOException | SQLException e) {
            logger.error("Could not query server table", e);
            return Collections.emptyList();
        }
    }

    @GET
    @Path("add")
    @Produces(MediaType.APPLICATION_JSON)
    public Object add(@QueryParam("name") String name, @QueryParam("address") String address, @QueryParam("port") int port) {

        logger.info("Requested addition: name: {}, address: {}, port:{}", name, address, port);

        try {
            if (name == null) {
                return "No name specified";
            }
            if (name.length() < 3 || name.length() > 20) {
                return "Name length must be in [3..20]";
            }
            if (port < 1024 || port > 65535) {
                return "Port must be in [1024..65535]";
            }
            InetAddress byName = InetAddress.getByName(address);
            if (!byName.isReachable(5000)) {
                return "Unreachable host: " + address + " (" + byName.getHostAddress() + ")";
            }

            ServerTable.insert(dataSource, "servers", name, address, port);

        } catch (UnknownHostException e) {
            return "Unknown host: " + address;
        } catch (IOException e) {
            return "IOExeption";
        } catch (SQLException e) {
            return "SQLException";
        }
        return "Server added";
    }

    @GET
    @Path("remove")
    @Produces(MediaType.APPLICATION_JSON)
    public Object remove(@QueryParam("name") String name, @QueryParam("address") String address, @QueryParam("port") int port) {
        try {
            ServerTable.remove(dataSource, "servers", address, port);
        } catch (SQLException e) {
            return "FAILED";
        }
        return "SUCCESS";
    }
}
