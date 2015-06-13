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

package org.terasology.web.artifactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class ArtifactoryRepo {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactoryRepo.class);

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            .create();

    private final File cacheFile;
    private final List<ArtifactInfo> artifactInfo;
    private final Map<String, ArtifactoryModule> moduleInfo;

    public ArtifactoryRepo(String uri, String repo, Path cacheFolder) throws IOException {
        cacheFile = cacheFolder.resolve(repo + "_cache.json").toFile();

        if (cacheFile.exists()) {
            try (Reader reader = Files.newReader(cacheFile, StandardCharsets.UTF_8)) {
                Type listType = new TypeToken<Map<String, ArtifactoryModule>>() { }.getType();
                moduleInfo = GSON.fromJson(reader, listType);
            }
        } else {
            moduleInfo = new HashMap<>();
        }

        artifactInfo = new ArrayList<>();

        String url = uri
                + "/api/storage"
                + "/" + repo
                + "/org/terasology/modules";

        ArtifactoryItem folder = readItem(url);
        for (ArtifactoryItem.Entry child : folder.children) {
            if (child.folder) {
                String moduleUrl = url + child.uri;
                String moduleName = child.uri.substring(1);

                ArtifactoryItem mavenMeta = readItem(moduleUrl + "/maven-metadata.xml");
                ArtifactoryModule cachedMod = moduleInfo.get(moduleName);
                if (cachedMod == null) {
                    cachedMod = new ArtifactoryModule();
                    cachedMod.lastModified = new Date(0);
                    cachedMod.items = new ArrayList<>();
                    moduleInfo.put(moduleName, cachedMod);
                }
                if (mavenMeta.lastModified.after(cachedMod.lastModified)) {
                    cachedMod.lastModified = mavenMeta.lastModified;
                    cachedMod.items.clear();

                    ArtifactoryItem moduleFolder = readItem(moduleUrl);
                    for (ArtifactoryItem.Entry child2 : moduleFolder.children) {
                        if (child2.folder) {
                            String versionUrl = moduleUrl + child2.uri;
                            ArtifactoryItem versionFolder = readItem(versionUrl);
                            for (ArtifactoryItem.Entry child3 : versionFolder.children) {
                                if (matches(child3.uri)) {
                                    String artifactUrl = versionUrl + child3.uri;
                                    ArtifactoryItem artifact = readItem(artifactUrl);
                                    artifactInfo.add(new ArtifactoryArtifactInfo(artifact));
                                    cachedMod.items.add(new ArtifactoryArtifactInfo(artifact));
                                    logger.debug("Added " + artifactUrl);
                                }
                            }
                        }
                    }
                    logger.info("Updated " + moduleName);
                } else {
                    artifactInfo.addAll(cachedMod.items);
                }
            }
        }

        try (Writer writer = Files.newWriter(cacheFile, StandardCharsets.UTF_8)) {
            GSON.toJson(moduleInfo, writer);
        }
    }

    public Collection<ArtifactInfo> getModuleArtifacts() {
        return artifactInfo;
    }

    private static ArtifactoryItem readItem(String url) throws IOException {
        try (Reader reader = new InputStreamReader(new URL(url).openStream(), StandardCharsets.UTF_8)) {
            ArtifactoryItem folder = GSON.fromJson(reader, ArtifactoryItem.class);
            return folder;
        }
    }

    private static boolean matches(String uri) {
        if (uri.endsWith(".jar")) {
            if (!uri.endsWith("-sources.jar") && !uri.endsWith("-javadoc.jar")) {
                return true;
            }
        }
        return false;
    }
}
