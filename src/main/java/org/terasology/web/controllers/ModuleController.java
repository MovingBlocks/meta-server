// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.web.controllers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.views.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.module.Module;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;
import org.terasology.module.RemoteModuleExtension;
import org.terasology.naming.Name;
import org.terasology.naming.Version;
import org.terasology.naming.exception.VersionParseException;
import org.terasology.web.services.api.ModuleListService;
import org.terasology.web.version.VersionInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO Type description
 */
@Controller("/modules/")
@Produces(MediaType.TEXT_HTML)
public class ModuleController {

    private static final Logger logger = LoggerFactory.getLogger(ModuleController.class);

    private final ModuleListService model;

    private final ModuleMetadataJsonAdapter metadataWriter;

    /**
     * Sorts modules descending by version - id is ignored
     */
    private final Comparator<Module> versionComparator = (m1, m2) -> m2.getVersion().compareTo(m1.getVersion());

    public ModuleController(
            ModuleListService model
    ) {
        this.model = model;
        this.metadataWriter = new ModuleMetadataJsonAdapter();
        for (RemoteModuleExtension ext : RemoteModuleExtension.values()) {
            metadataWriter.registerExtension(ext.getKey(), ext.getValueType());
        }
    }

    @Get("show")
    @View("module-list")
    public HttpResponse<Map<Object, Object>> show() {
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

    @Get("show/{module}")
    @View("module-list")
    public HttpResponse<Map<Object, Object>> showModule(@PathVariable("module") String module) {
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
    public HttpResponse<Map<Object, Object>> listModuleLatest(HttpRequest<?> httpRequest, @PathVariable("module") String module) {
        URI uri = httpRequest.getUri();
        logger.info("Requested lastest module info as HTML");
        int pathLen = uri.getPath().length();
        String path = uri.getPath().substring(0, pathLen - "latest".length());
        Module latest = model.getLatestModuleVersion(new Name(module));
        if (latest == null) {
            return HttpResponse.notFound(Map.of("version", VersionInfo.getVersion()));
        }
        String ver = latest.getVersion().toString();
        URI redirect = URI.create(path + ver);
        return HttpResponse.temporaryRedirect(redirect);
    }

    @Get("show/{module}/latest")
    public HttpResponse<Map<Object, Object>> showModuleLatest(HttpRequest<?> httpRequest, @PathVariable("module") String module) {
        URI uriInfo = httpRequest.getUri();
        logger.info("Requested lastest module info as HTML");
        int pathLen = uriInfo.getPath().length();
        String path = uriInfo.getPath().substring(0, pathLen - "latest".length());
        Module latest = model.getLatestModuleVersion(new Name(module));
        if (latest == null) {
            return HttpResponse.notFound(Map.of("version", VersionInfo.getVersion()));
        }
        String ver = latest.getVersion().toString();
        URI redirect = URI.create(path + ver);
        return HttpResponse.temporaryRedirect(redirect);
    }

    @Get("show/{module}/{version}")
    @View("module-info")
    public HttpResponse<Map<Object, Object>> showModuleVersion(@PathVariable("module") String module, @PathVariable("version") String version) {
        logger.info("Requested module info as HTML");

        try {
            Name moduleName = new Name(module);
            Version modVersion = new Version(version);
            Module mod = model.getModule(moduleName, modVersion);
            if (mod == null) {
                logger.warn("No entry for module '{}' found", module);
                return HttpResponse.notFound(Map.of("version", VersionInfo.getVersion()));
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
            return HttpResponse.notFound(Map.of("version", VersionInfo.getVersion()));
        }
    }
}
