// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.web.controllers.api;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.web.model.server.ServerEntry;
import org.terasology.web.services.api.ServerListService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Controller("/servers/")
@Produces(MediaType.APPLICATION_JSON)
public class ApiServerController {

    private static final Logger logger = LoggerFactory.getLogger(ApiServerController.class);

    private final ServerListService model;

    public ApiServerController(ServerListService model) {
        this.model = model;
    }

    @Get("list")
    public List<ServerEntry> list() {
        logger.info("Requested server list");
        try {
            return model.getServers();
        } catch (IOException e) {
            logger.error("Could not connect to database", e);
            return Collections.emptyList();
        }
    }
}
