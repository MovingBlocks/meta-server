// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.web.controllers.api;

import com.google.gson.stream.JsonWriter;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.module.Module;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;
import org.terasology.module.RemoteModuleExtension;
import org.terasology.naming.Name;
import org.terasology.naming.Version;
import org.terasology.naming.exception.VersionParseException;
import org.terasology.web.model.jenkins.Job;
import org.terasology.web.services.api.ModuleListService;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

@Controller("/modules/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiModuleController {

    private static final Logger logger = LoggerFactory.getLogger(ApiModuleController.class);

    private final ModuleListService model;

    private final ModuleMetadataJsonAdapter metadataWriter;

    public ApiModuleController(
            ModuleListService model
    ) {
        this.model = model;
        this.metadataWriter = new ModuleMetadataJsonAdapter();
        for (RemoteModuleExtension ext : RemoteModuleExtension.values()) {
            metadataWriter.registerExtension(ext.getKey(), ext.getValueType());
        }
    }

    @Get("list")
    public HttpResponse<String> list() {
        logger.info("Requested module list as json");

        List<ModuleMetadata> sortedModuleMetadatas = model.getModuleIds()
                .stream()
                .sorted()
                .flatMap(name -> model.getModuleVersions(name).stream())
                .map(Module::getMetadata)
                .collect(Collectors.toList());

        return createResponse(sortedModuleMetadatas);
    }


    @Get("list/latest")
    public HttpResponse<String> listLatest() {
        logger.info("Requested lastest info as json");

        List<ModuleMetadata> sortedModuleMetadatas = model.getModuleIds()
                .stream()
                .sorted()
                .map(model::getLatestModuleVersion)
                .map(Module::getMetadata)
                .collect(Collectors.toList());

        return createResponse(sortedModuleMetadatas);
    }

    @Get("list/{module}")
    public HttpResponse<String> listModule(@PathVariable("module") String moduleName) {
        logger.info("Requested module versions as json");

        Name name = new Name(moduleName);
        List<ModuleMetadata> moduleMetadatas = model.getModuleVersions(name)
                .stream().map(Module::getMetadata)
                .collect(Collectors.toList());

        return createResponse(moduleMetadatas);
    }

    @Get("list/{module}/{version}")
    public HttpResponse<String> listModuleVersion(@PathVariable("module") String moduleName, @PathVariable("version") String versionStr) {
        logger.info("Requested single module info as json");
        try {
            Version version = new Version(versionStr);
            Module module = model.getModule(new Name(moduleName), version);
            if (module != null) {
                ModuleMetadata meta = module.getMetadata();
                StringWriter response = new StringWriter();

                metadataWriter.write(meta, response);
                return HttpResponse.ok(response.toString());

            } else {
                return HttpResponse.notFound();
            }

        } catch (VersionParseException e) {
            logger.warn("Invalid version for module '{}' specified: {}", moduleName, versionStr);
            return HttpResponse.notFound();
        }
    }

    @Post("update")
    public HttpResponse<String> updateModulePost(Job jobState) {
        String job = jobState.getName();

        logger.info("Requested module update for {}", job);

        model.updateModule(new Name(job));

        return HttpResponse.ok();
    }

    @Post("update-all")
    public HttpResponse<String> updateAllModulesPost() {

        logger.info("Requested complete module update");

        new Thread(model::updateAllModules).start();

        return HttpResponse.ok();
    }

    private HttpResponse<String> createResponse(List<ModuleMetadata> sortedModuleMetadatas) {
        StringWriter response = new StringWriter();
        try (JsonWriter writer = new JsonWriter(response)) {
            writer.beginArray();
            writer.setIndent("  "); // enable pretty printing
            for (ModuleMetadata meta : sortedModuleMetadatas) {
                metadataWriter.write(meta, writer);
            }
            writer.endArray();
        } catch (IOException e) {
            logger.error("Cannot create module list", e);
            return HttpResponse.serverError();
        }
        return HttpResponse.ok(response.toString());
    }
}
