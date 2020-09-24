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
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.google.gson.stream.JsonWriter;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.server.util.HttpHostResolver;
import io.micronaut.views.View;
import io.micronaut.web.router.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.module.Module;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;
import org.terasology.module.RemoteModuleExtension;
import org.terasology.naming.Name;
import org.terasology.naming.Version;
import org.terasology.naming.exception.VersionParseException;
import org.terasology.web.model.ModuleListModel;
import org.terasology.web.model.jenkins.Job;
import org.terasology.web.version.VersionInfo;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;

/**
 * TODO Type description
 */
@Controller("/modules/")
public class ModuleServlet {

    private static final Logger logger = LoggerFactory.getLogger(ModuleServlet.class);

    private final ModuleListModel model;

    private final ModuleMetadataJsonAdapter metadataWriter;

    /**
     * Sorts modules descending by version - id is ignored
     */
    private final Comparator<Module> versionComparator = (m1, m2) -> m2.getVersion().compareTo(m1.getVersion());

    private final HttpHostResolver httpHostResolver;
    private final RouteBuilder.UriNamingStrategy uriNamingStrategy;

    public ModuleServlet(
            HttpHostResolver httpHostResolver,
            RouteBuilder.UriNamingStrategy uriNamingStrategy,
            ModuleListModel model
    ) {
        this.model = model;
        this.httpHostResolver = httpHostResolver;
        this.uriNamingStrategy = uriNamingStrategy;
        this.metadataWriter = new ModuleMetadataJsonAdapter();
        for (RemoteModuleExtension ext : RemoteModuleExtension.values()) {
            metadataWriter.registerExtension(ext.getKey(), ext.getValueType());
        }
    }

    @Get("list")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse list() {
        logger.info("Requested module list as json");
        StringWriter response = new StringWriter();
        List<Name> sortedModuleIds = new ArrayList<>(model.getModuleIds());
        sortedModuleIds.sort(null);
        try (JsonWriter writer = new JsonWriter(response)) {
            writer.beginArray();
            writer.setIndent("  "); // enable pretty printing
            for (Name name : sortedModuleIds) {
                for (Module module : model.getModuleVersions(name)) {
                    ModuleMetadata meta = module.getMetadata();
                    metadataWriter.write(meta, writer);
                }
            }
            writer.endArray();
        } catch (IOException e) {
            logger.error("Cannot create module list", e);
            return HttpResponse.serverError();
        }
        return HttpResponse.ok(response.toString());
    }

    @Get("show")
    @View("module-list")
    @Produces(MediaType.TEXT_HTML)
    public HttpResponse show() {
        logger.info("Requested module list as HTML");

        Set<Name> names = model.getModuleIds();

        // the key needs to be string, so that FreeMarker can use it for lookups
        Multimap<String, Module> map = TreeMultimap.create(String.CASE_INSENSITIVE_ORDER, versionComparator);

        for (Name name : names) {
            map.putAll(name.toString(), model.getModuleVersions(name));
        }

        ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                .put("items", map.asMap())
                .put("version", VersionInfo.getVersion())
                .build();
        return HttpResponse.ok(dataModel);
    }

    @Get("list/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse listLatest() {
        logger.info("Requested lastest info as json");
        StringWriter response = new StringWriter();
        List<Name> sortedModuleIds = new ArrayList<>(model.getModuleIds());
        sortedModuleIds.sort(null);
        try (JsonWriter writer = new JsonWriter(response)) {
            writer.beginArray();
            writer.setIndent("  "); // enable pretty printing
            for (Name name : sortedModuleIds) {
                Module module = model.getLatestModuleVersion(name);
                ModuleMetadata meta = module.getMetadata();
                metadataWriter.write(meta, writer);
            }
            writer.endArray();
        } catch (IOException e) {
            logger.error("Cannot create module list", e);
            return HttpResponse.serverError();
        }
        return HttpResponse.ok(response.toString());
    }

    @Get("list/{module}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse listModule(@PathVariable("module") String moduleName) {
        logger.info("Requested module versions as json");

        Name name = new Name(moduleName);
        StringWriter response = new StringWriter();
        try (JsonWriter writer = new JsonWriter(response)) {
            writer.beginArray();
            writer.setIndent("  "); // enable pretty printing
            for (Module module : model.getModuleVersions(name)) {
                ModuleMetadata meta = module.getMetadata();
                metadataWriter.write(meta, writer);
            }
            writer.endArray();
        } catch (IOException e) {
            logger.error("Cannot create module list", e);
            return HttpResponse.serverError();
        }
        return HttpResponse.ok(response.toString());
    }

    @Get("show/{module}")
    @View("module-list")
    @Produces(MediaType.TEXT_HTML)
    public HttpResponse showModule(@PathVariable("module") String module) {
        logger.info("Requested module versions as HTML");

        Name name = new Name(module);

        List<Module> sortedList = new ArrayList<>(model.getModuleVersions(name));
        sortedList.sort(versionComparator);

        Map<String, Collection<Module>> map = Collections.singletonMap(module, sortedList);

        ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                .put("items", map)
                .put("moduleId", module)
                .put("version", VersionInfo.getVersion())
                .build();
        return HttpResponse.ok(dataModel);
    }

    @Get("list/{module}/latest")
    @Produces(MediaType.TEXT_HTML)
    public HttpResponse listModuleLatest(HttpRequest httpRequest, @PathVariable("module") String module) {
        URI uri = httpRequest.getUri();
        logger.info("Requested lastest module info as HTML");
        int pathLen = uri.getPath().length();
        String path = uri.getPath().substring(0, pathLen - "latest".length());
        Module latest = model.getLatestModuleVersion(new Name(module));
        if (latest == null) {
            return HttpResponse.notFound();
        }
        String ver = latest.getVersion().toString();
        URI redirect = URI.create(path + ver);
        return HttpResponse.temporaryRedirect(redirect);
    }

    @Get("list/{module}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse listModuleVersion(@PathVariable("module") String moduleName, @PathVariable("version") String versionStr) {
        logger.info("Requested single module info as json");

        try {
            Version version = new Version(versionStr);
            Module module = model.getModule(new Name(moduleName), version);
            if (module == null) {
                return HttpResponse.notFound();
            }

            ModuleMetadata meta = module.getMetadata();
            StringWriter response = new StringWriter();

            metadataWriter.write(meta, response);
            return HttpResponse.ok(response.toString());
        } catch (VersionParseException e) {
            logger.warn("Invalid version for module '{}' specified: {}", moduleName, versionStr);
            return HttpResponse.notFound();
        }
    }

    @Get("show/{module}/latest")
    @Produces(MediaType.TEXT_HTML)
    public HttpResponse showModuleLatest(HttpRequest httpRequest, @PathVariable("module") String module) {
        URI uriInfo = httpRequest.getUri();
        logger.info("Requested lastest module info as HTML");
        int pathLen = uriInfo.getPath().length();
        String path = uriInfo.getPath().substring(0, pathLen - "latest".length());
        Module latest = model.getLatestModuleVersion(new Name(module));
        if (latest == null) {
            return HttpResponse.notFound();
        }
        String ver = latest.getVersion().toString();
        URI redirect = URI.create(path + ver);
        return HttpResponse.temporaryRedirect(redirect);
    }

    @Get("show/{module}/{version}")
    @View("module-info")
    @Produces(MediaType.TEXT_HTML)
    public HttpResponse showModuleVersion(@PathVariable("module") String module, @PathVariable("version") String version) {
        logger.info("Requested module info as HTML");

        try {
            Name moduleName = new Name(module);
            Version modVersion = new Version(version);
            Module mod = model.getModule(moduleName, modVersion);
            if (mod == null) {
                logger.warn("No entry for module '{}' found", module);
                return HttpResponse.notFound();
            }
            ModuleMetadata meta = mod.getMetadata();

            Set<Module> deps = model.resolve(moduleName, modVersion);

            ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                    .put("meta", meta)
                    .put("updated", RemoteModuleExtension.getLastUpdated(meta))
                    .put("downloadUrl", RemoteModuleExtension.getDownloadUrl(meta))
                    .put("downloadSize", RemoteModuleExtension.getArtifactSize(meta) / 1024)
                    .put("dependencies", deps)
                    .put("version", VersionInfo.getVersion())
                    .build();
            return HttpResponse.ok(dataModel);
        } catch (VersionParseException e) {
            logger.warn("Invalid version for module '{}' specified: {}", module, version);
            return HttpResponse.notFound();
        }
    }

    @Post("update")
    @Consumes(MediaType.APPLICATION_JSON)
    public HttpResponse updateModulePost(Job jobState) {
        String job = jobState.getName();

        logger.info("Requested module update for {}", job);

        model.updateModule(new Name(job));

        return HttpResponse.ok();
    }

    @Post("update-all")
    @Consumes(MediaType.APPLICATION_JSON)
    public HttpResponse updateAllModulesPost() {

        logger.info("Requested complete module update");

        new Thread(model::updateAllModules).start();

        return HttpResponse.ok();
    }
}
