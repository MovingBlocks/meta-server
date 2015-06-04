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
import java.util.List;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class ArtifactoryRepo {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final File cacheFile;
    private final List<ArtifactoryItem> artifactInfo;

    public ArtifactoryRepo(String uri, String repo, Path cacheFolder) throws IOException {
        cacheFile = cacheFolder.resolve(repo + "_cache.json").toFile();

        if (cacheFile.exists()) {
            try (Reader reader = Files.newReader(cacheFile, StandardCharsets.UTF_8)) {
                Type listType = new TypeToken<List<ArtifactoryItem>>() { }.getType();
                artifactInfo = GSON.fromJson(reader, listType);
                return;
            }
        }

        artifactInfo = new ArrayList<>();

        String url = uri
                + "/api/storage"
                + "/" + repo
                + "/org/terasology/modules";

        ArtifactoryItem folder = readFolder(url);
        for (ArtifactoryItem.Entry child : folder.children) {
            if (child.folder) {
                String moduleUrl = url + child.uri;
                ArtifactoryItem moduleFolder = readFolder(moduleUrl);
                for (ArtifactoryItem.Entry child2 : moduleFolder.children) {
                    if (child2.folder) {
                        String versionUrl = moduleUrl + child2.uri;
                        ArtifactoryItem versionFolder = readFolder(versionUrl);
                        for (ArtifactoryItem.Entry child3 : versionFolder.children) {
                            if (matches(child3.uri)) {
                                String artifactUrl = versionUrl + child3.uri;
                                ArtifactoryItem artifact = readFolder(artifactUrl);
                                artifactInfo.add(artifact);
                            }
                        }
                    }
                }
            }
        }

        cacheFolder.toFile().mkdirs();
        try (Writer writer = Files.newWriter(cacheFile, StandardCharsets.UTF_8)) {
            GSON.toJson(artifactInfo, writer);
        }
    }

    public List<ArtifactoryItem> getModules() {
        return artifactInfo;
    }

    private static ArtifactoryItem readFolder(String uri) throws IOException {
        try (Reader reader = new InputStreamReader(new URL(uri).openStream(), StandardCharsets.UTF_8)) {
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
