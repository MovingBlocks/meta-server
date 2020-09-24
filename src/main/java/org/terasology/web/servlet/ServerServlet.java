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

package org.terasology.web.servlet;

import com.google.common.collect.ImmutableMap;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.web.model.Result;
import org.terasology.web.model.ServerEntry;
import org.terasology.web.model.ServerListModel;
import org.terasology.web.version.VersionInfo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

/**
 *
 */
@Controller("/servers/")
public class ServerServlet {

    private static final Logger logger = LoggerFactory.getLogger(ServerServlet.class);

    private ServerListModel model;

    public ServerServlet(ServerListModel model) {
        this.model = model;
    }

    @Get("show")
    @View("server-list")
    @Produces(MediaType.TEXT_HTML)
    public HttpResponse show() {
        logger.info("Requested server list as HTML");
        ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                .put("items", list())
                .put("version", VersionInfo.getVersion())
                .build();
        return HttpResponse.ok(dataModel);
    }

    @Get("add")
    @View("add")
    @Produces(MediaType.TEXT_HTML)
    public HttpResponse add() {
        logger.info("Requested add as HTML");
        ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                .put("name", "")
                .put("address", "")
                .put("port", 25777)
                .put("owner", "")
                .put("active", false)
                .put("version", VersionInfo.getVersion())
                .build();
        return HttpResponse.ok(dataModel);
    }

    @Get("edit")
    @View("edit")
    @Produces(MediaType.TEXT_HTML)
    public HttpResponse edit(@QueryValue(value = "index", defaultValue = "-1") int index) throws IOException {
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
                .put("active", server.isActive())
                .put("version", VersionInfo.getVersion())
                .build();

        return HttpResponse.ok(dataModel);
    }

    @Get("list")
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

    @Post("add")
    @View("add")
    @Produces(MediaType.TEXT_HTML)
    public HttpResponse add(@Body ServerForm serverForm) throws URISyntaxException {

        boolean active = "on".equals(serverForm.getActiveOn());
        logger.info("Requested addition: name: {}, address: {}, port:{}, owner:{}, active:{}", serverForm.getName(), serverForm.getAddress(), serverForm.getPort(), serverForm.getOwner(), active);

        Result response = model.addServer(serverForm.getName(), serverForm.getAddress(), serverForm.getPort(), serverForm.getOwner(), active, serverForm.getSecret());

        if (response.isSuccess()) {
            ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                    .put("items", list())
                    .put("message", response.getMessage())
                    .put("version", VersionInfo.getVersion())
                    .build();
            return HttpResponse.redirect(new URI("list"));
        } else {
            ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                    .put("name", serverForm.getName())
                    .put("address", serverForm.getAddress())
                    .put("port", serverForm.getPort())
                    .put("owner", serverForm.getOwner())
                    .put("active", active)
                    .put("error", response.getMessage())
                    .put("version", VersionInfo.getVersion())
                    .build();
            return HttpResponse.ok(dataModel);
        }
    }

    @Post("remove")
    @View("edit")
    @Produces(MediaType.TEXT_HTML)
    public HttpResponse remove(@Body ServerForm serverForm) throws URISyntaxException {

        boolean active = "on".equals(serverForm.getActiveOn());
        Result response = model.removeServer(serverForm.getAddress(), serverForm.getPort(), serverForm.getSecret());
        if (response.isSuccess()) {
            ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                    .put("items", list())
                    .put("message", response.getMessage())
                    .put("version", VersionInfo.getVersion())
                    .build();
            return HttpResponse.redirect(new URI("list"));
        } else {
            ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                    .put("name", serverForm.getName())
                    .put("address", serverForm.getAddress())
                    .put("port", serverForm.getPort())
                    .put("owner", serverForm.getOwner())
                    .put("active", active)
                    .put("error", response.getMessage())
                    .put("version", VersionInfo.getVersion())
                    .build();
            return HttpResponse.ok(dataModel);
        }
    }

    @Post("update")
    @View("edit")
    @Produces(MediaType.TEXT_HTML)
    public HttpResponse update(@Body ServerForm serverForm) throws URISyntaxException {

        boolean active = "on".equals(serverForm.getActiveOn());
        Result response = model.updateServer(serverForm.getName(), serverForm.getAddress(), serverForm.getPort(), serverForm.getOwner(), active, serverForm.getActiveOn());
        if (response.isSuccess()) {
            ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                    .put("items", list())
                    .put("message", response.getMessage())
                    .put("version", VersionInfo.getVersion())
                    .build();
            return HttpResponse.redirect(new URI("list"));
        } else {
            ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                    .put("name", serverForm.getName())
                    .put("address", serverForm.getAddress())
                    .put("port", serverForm.getPort())
                    .put("owner", serverForm.getOwner())
                    .put("active", active)
                    .put("error", response.getMessage())
                    .put("version", VersionInfo.getVersion())
                    .build();
            return HttpResponse.ok(dataModel);
        }
    }
}
