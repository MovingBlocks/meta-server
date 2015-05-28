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
import java.util.Collections;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.mvc.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.version.Version;
import org.terasology.web.model.Result;
import org.terasology.web.model.ServerEntry;
import org.terasology.web.model.ServerListModel;

import com.google.common.collect.ImmutableMap;

/**
 * @author Martin Steiger
 */
@Path("/")
public class Serveletty {

    private static final Logger logger = LoggerFactory.getLogger(Serveletty.class);

    private ServerListModel model;

    public Serveletty(ServerListModel model) {
        this.model = model;
    }

    @GET
    @Path("show")
    @Produces(MediaType.TEXT_HTML)
    public Viewable show() {
        logger.info("Requested server list as HTML");
        ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                .put("items", list())
                .put("version", Version.getVersion())
                .build();
        return new Viewable("/server-list.ftl", dataModel);
    }

    @GET
    @Path("add")
    @Produces(MediaType.TEXT_HTML)
    public Viewable add() {
        logger.info("Requested add as HTML");
        ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                .put("name", "")
                .put("address", "")
                .put("port", 25777)
                .put("owner", "")
                .put("version", Version.getVersion())
                .build();
        return new Viewable("/add.ftl", dataModel);
    }

    @GET
    @Path("edit")
    @Produces(MediaType.TEXT_HTML)
    public Viewable edit(@QueryParam("index") @DefaultValue("-1") int index) throws IOException {
        List<ServerEntry> servers = model.getServers();

        if (index < 0 || index >= servers.size()) {
            return null;
        }

        ServerEntry server = servers.get(index);

        ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                .put("name", server.getName())
                .put("address", server.getAddress())
                .put("port", server.getPort())
                .put("owner", server.getOwner())
                .put("version", Version.getVersion())
                .build();

        return new Viewable("/edit.ftl", dataModel);
    }

    @GET
    @Path("about")
    @Produces(MediaType.TEXT_HTML)
    public Viewable about() {
        logger.info("Requested about as HTML");
        ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
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
            return model.getServers();
        } catch (IOException e) {
            logger.error("Could not connect to database", e);
            return Collections.emptyList();
        }
    }

    @POST
    @Path("add")
    @Produces(MediaType.TEXT_HTML)
    public Viewable add(@FormParam("name") String name, @FormParam("address") String address, @FormParam("port") int port,
            @FormParam("owner") String owner, @FormParam("secret") String secret) {

        logger.info("Requested addition: name: {}, address: {}, port:{}, owner:{}", name, address, port, owner);

        Result response = model.addServer(name, address, port, owner, secret);

        if (response.isSuccess()) {
            ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                    .put("items", list())
                    .put("message", response.getMessage())
                    .put("version", Version.getVersion())
                    .build();
            return new Viewable("/server-list.ftl", dataModel);
        } else {
            ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                    .put("name", name)
                    .put("address", address)
                    .put("port", port)
                    .put("owner", owner)
                    .put("error", response.getMessage())
                    .put("version", Version.getVersion())
                    .build();
            return new Viewable("/add.ftl", dataModel);
        }
    }

    @POST
    @Path("remove")
    @Produces(MediaType.TEXT_HTML)
    public Viewable remove(@FormParam("name") String name, @FormParam("address") String address, @FormParam("port") int port,
            @FormParam("owner") String owner, @FormParam("secret") String secret) {

        Result response = model.removeServer(address, port, secret);
        if (response.isSuccess()) {
            ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                    .put("items", list())
                    .put("message", response.getMessage())
                    .put("version", Version.getVersion())
                    .build();
            return new Viewable("/server-list.ftl", dataModel);
        } else {
            ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                    .put("name", name)
                    .put("address", address)
                    .put("port", port)
                    .put("owner", owner)
                    .put("error", response.getMessage())
                    .put("version", Version.getVersion())
                    .build();
            return new Viewable("/edit.ftl", dataModel);
        }
    }

    @POST
    @Path("update")
    @Produces(MediaType.TEXT_HTML)
    public Viewable update(@FormParam("name") String name, @FormParam("address") String address, @FormParam("port") int port,
            @FormParam("owner") String owner, @FormParam("secret") String secret) {

        Result response = model.updateServer(name, address, port, owner, secret);
        if (response.isSuccess()) {
            ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                    .put("items", list())
                    .put("message", response.getMessage())
                    .put("version", Version.getVersion())
                    .build();
            return new Viewable("/server-list.ftl", dataModel);
        } else {
            ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                    .put("name", name)
                    .put("address", address)
                    .put("port", port)
                    .put("owner", owner)
                    .put("error", response.getMessage())
                    .put("version", Version.getVersion())
                    .build();
            return new Viewable("/edit.ftl", dataModel);
        }
    }
}
