// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.web.controllers;

import com.google.common.collect.ImmutableMap;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.ModelAndView;
import io.micronaut.views.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.web.model.server.Result;
import org.terasology.web.model.server.ServerEntry;
import org.terasology.web.model.web.ServerForm;
import org.terasology.web.services.api.ServerListService;
import org.terasology.web.version.VersionInfo;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Controller which servers WEB for meta-server
 */
@Controller("/servers/")
@Produces(MediaType.TEXT_HTML)
public class ServerController {

    private static final Logger logger = LoggerFactory.getLogger(ServerController.class);

    private final ServerListService model;

    public ServerController(ServerListService model) {
        this.model = model;
    }

    @Get("show")
    @View("server-list")
    public HttpResponse<Map<Object, Object>> show() {
        logger.info("Requested server list as HTML");
        ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                .put("items", list())
                .put("version", VersionInfo.getVersion())
                .build();
        return HttpResponse.ok(dataModel);
    }

    @Get("add")
    @View("add")
    public HttpResponse<Map<Object, Object>> add() {
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
    public HttpResponse<Map<Object, Object>> edit(@QueryValue(value = "index", defaultValue = "-1") int index) throws IOException {
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

    @Post("add")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public ModelAndView<Map<Object, Object>> add(@Body ServerForm serverForm) {

        boolean active = "on".equals(serverForm.getActiveOn());
        logger.info("Requested addition: name: {}, address: {}, port:{}, owner:{}, active:{}",
                serverForm.getName(),
                serverForm.getAddress(),
                serverForm.getPort(),
                serverForm.getOwner(),
                active);

        Result response = model.addServer(
                serverForm.getName(),
                serverForm.getAddress(),
                serverForm.getPort(),
                serverForm.getOwner(),
                active,
                serverForm.getSecret());

        if (response.isSuccess()) {
            ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                    .put("items", list())
                    .put("message", response.getMessage())
                    .put("version", VersionInfo.getVersion())
                    .build();
            return new ModelAndView<>("list", dataModel);
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
            return new ModelAndView<>("add", dataModel);
        }
    }

    @Post("remove")
    public ModelAndView<Map<Object, Object>> remove(@Body ServerForm serverForm) {

        boolean active = "on".equals(serverForm.getActiveOn());
        Result response = model.removeServer(serverForm.getAddress(), serverForm.getPort(), serverForm.getSecret());
        if (response.isSuccess()) {
            ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                    .put("items", list())
                    .put("message", response.getMessage())
                    .put("version", VersionInfo.getVersion())
                    .build();
            return new ModelAndView<>("list", dataModel);
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
            return new ModelAndView<>("edit", dataModel);
        }
    }

    @Post("update")
    public ModelAndView<Map<Object, Object>> update(@Body ServerForm serverForm) {

        boolean active = "on".equals(serverForm.getActiveOn());
        Result response = model.updateServer(serverForm.getName(), serverForm.getAddress(), serverForm.getPort(), serverForm.getOwner(), active, serverForm.getActiveOn());
        if (response.isSuccess()) {
            ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                    .put("items", list())
                    .put("message", response.getMessage())
                    .put("version", VersionInfo.getVersion())
                    .build();
            return new ModelAndView<>("list", dataModel);
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
            return new ModelAndView<>("edit", dataModel);
        }
    }

    private List<ServerEntry> list() {
        logger.info("Requested server list");
        try {
            return model.getServers();
        } catch (IOException e) {
            logger.error("Could not connect to database", e);
            return Collections.emptyList();
        }
    }
}
