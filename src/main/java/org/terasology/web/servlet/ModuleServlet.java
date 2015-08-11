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

import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.mvc.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.module.Module;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;
import org.terasology.module.RemoteModuleExtension;
import org.terasology.naming.Name;
import org.terasology.naming.Version;
import org.terasology.naming.exception.VersionParseException;
import org.terasology.web.version.VersionInfo;
import org.terasology.web.model.ModuleListModel;
import org.terasology.web.model.jenkins.Job;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.google.gson.stream.JsonWriter;

/**
 * TODO Type description
 */
@Path("/modules/")
public class ModuleServlet {

    private static final Logger logger = LoggerFactory.getLogger(ModuleServlet.class);

    private final ModuleListModel model;

    private final ModuleMetadataJsonAdapter metadataWriter;

    /**
     * Sorts modules descending by version - id is ignored
     */
    private final Comparator<Module> versionComparator = (m1, m2) -> m2.getVersion().compareTo(m1.getVersion());


    public ModuleServlet(ModuleListModel model) {
        this.model = model;
        this.metadataWriter = new ModuleMetadataJsonAdapter();
        for (RemoteModuleExtension ext : RemoteModuleExtension.values()) {
            metadataWriter.registerExtension(ext.getKey(), ext.getValueType());
        }
    }

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        logger.info("Requested module list as json");

        StreamingOutput stream = os -> {
            List<Name> sortedModuleIds = new ArrayList<>(model.getModuleIds());
            sortedModuleIds.sort(null);
            try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                writer.beginArray();
                writer.setIndent("  "); // enable pretty printing
                for (Name name : sortedModuleIds) {
                    for (Module module : model.getModuleVersions(name)) {
                        ModuleMetadata meta = module.getMetadata();
                        metadataWriter.write(meta, writer);
                    }
                }
                writer.endArray();
            }
        };
        return Response.ok(stream).build();
    }

    @GET
    @Path("show")
    @Produces(MediaType.TEXT_HTML)
    public Viewable show() {
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
        return new Viewable("/module-list.ftl", dataModel);
    }

    @GET
    @Path("list/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listLatest() {
        logger.info("Requested lastest info as json");

        StreamingOutput stream = os -> {
            List<Name> sortedModuleIds = new ArrayList<>(model.getModuleIds());
            sortedModuleIds.sort(null);
            try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                writer.beginArray();
                writer.setIndent("  "); // enable pretty printing
                for (Name name : sortedModuleIds) {
                    Module module = model.getLatestModuleVersion(name);
                    ModuleMetadata meta = module.getMetadata();
                    metadataWriter.write(meta, writer);
                }
                writer.endArray();
            }
        };
        return Response.ok(stream).build();
    }

    @GET
    @Path("list/{module}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listModule(@PathParam("module") String moduleName) {
        logger.info("Requested module versions as json");

        Name name = new Name(moduleName);

        StreamingOutput stream = os -> {
            try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                writer.beginArray();
                writer.setIndent("  "); // enable pretty printing
                for (Module module : model.getModuleVersions(name)) {
                    ModuleMetadata meta = module.getMetadata();
                    metadataWriter.write(meta, writer);
                }
                writer.endArray();
            }
        };
        return Response.ok(stream).build();
    }

    @GET
    @Path("show/{module}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable showModule(@PathParam("module") String module) {
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
        return new Viewable("/module-list.ftl", dataModel);
    }

    @GET
    @Path("list/{module}/latest")
    @Produces(MediaType.TEXT_HTML)
    public Response listModuleLatest(@Context UriInfo uriInfo, @PathParam("module") String module) {
        logger.info("Requested lastest module info as HTML");
        int pathLen = uriInfo.getPath().length();
        String path = uriInfo.getPath().substring(0, pathLen - "latest".length());
        Module latest = model.getLatestModuleVersion(new Name(module));
        if (latest == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        String ver = latest.getVersion().toString();
        URI redirect = URI.create(uriInfo.getBaseUri() + path + ver);
        return Response.temporaryRedirect(redirect).build();
    }

    @GET
    @Path("list/{module}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listModuleVersion(@PathParam("module") String moduleName, @PathParam("version") String versionStr) {
        logger.info("Requested single module info as json");

        try {
            Version version = new Version(versionStr);
            Module module = model.getModule(new Name(moduleName), version);
            if (module == null) {
                return Response.status(Status.NOT_FOUND).build();
            }

            ModuleMetadata meta = module.getMetadata();

            StreamingOutput stream = os -> {
                try (OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                    metadataWriter.write(meta, writer);
                }
            };
            return Response.ok(stream).build();
        } catch (VersionParseException e) {
            logger.warn("Invalid version for module '{}' specified: {}", moduleName, versionStr);
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("show/{module}/latest")
    @Produces(MediaType.TEXT_HTML)
    public Response showModuleLatest(@Context UriInfo uriInfo, @PathParam("module") String module) {
        logger.info("Requested lastest module info as HTML");
        int pathLen = uriInfo.getPath().length();
        String path = uriInfo.getPath().substring(0, pathLen - "latest".length());
        Module latest = model.getLatestModuleVersion(new Name(module));
        if (latest == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        String ver = latest.getVersion().toString();
        URI redirect = URI.create(uriInfo.getBaseUri() + path + ver);
        return Response.temporaryRedirect(redirect).build();
    }

    @GET
    @Path("show/{module}/{version}")
    @Produces(MediaType.TEXT_HTML)
    public Response showModuleVersion(@PathParam("module") String module, @PathParam("version") String version) {
        logger.info("Requested module info as HTML");

        try {
            Name moduleName = new Name(module);
            Version modVersion = new Version(version);
            Module mod = model.getModule(moduleName, modVersion);
            if (mod == null) {
                logger.warn("No entry for module '{}' found", module);
                return Response.status(Status.NOT_FOUND).build();
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
            return Response.ok(new Viewable("/module-info.ftl", dataModel)).build();
        } catch (VersionParseException e) {
            logger.warn("Invalid version for module '{}' specified: {}", module, version);
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path("update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateModulePost(Job jobState) {
        String job = jobState.getName();

        logger.info("Requested module update for {}", job);

        model.updateModule(new Name(job));

        return Response.ok().build();
    }

    @POST
    @Path("update-all")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateAllModulesPost() {

        logger.info("Requested complete module update");

        new Thread(model::updateAllModules).start();

        return Response.ok().build();
    }
}
