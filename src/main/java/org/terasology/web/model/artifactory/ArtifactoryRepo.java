// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.web.model.artifactory;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implements {@link ArtifactRepository} for Artifactory.
 */
public final class ArtifactoryRepo implements ArtifactRepository {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactoryRepo.class);

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            .create();

    private final Map<String, Collection<ArtifactoryArtifactInfo>> artifactInfo = new LinkedHashMap<>();

    private final String baseUrl;
    private final Path cacheFolder;
    private final String repoName;
    private final String group;
    private final RepoType type;

    private ArtifactoryRepo(String uri, String repoName, String group, Path cacheFolder, RepoType type) throws IOException {
        this.cacheFolder = cacheFolder;
        this.repoName = repoName;
        this.type = type;
        this.group = group;
        String repoPath = group.replaceAll("\\.", "/");

        baseUrl = uri
                + "/api/storage"
                + "/" + repoName
                + "/" + repoPath;

        ArtifactoryItem folder = readItem(baseUrl);
        for (ArtifactoryItem.Entry child : folder.children) {
            if (child.folder) {
                String moduleName = child.uri.substring(1);
                artifactInfo.put(moduleName, Collections.emptySet());
            }
        }
    }

    public static ArtifactoryRepo snapshot(String uri, String repoName, String group, Path cacheFolder) throws IOException {
        return new ArtifactoryRepo(uri, repoName, group, cacheFolder, RepoType.SNAPSHOT);
    }

    public static ArtifactoryRepo release(String uri, String repoName, String group, Path cacheFolder) throws IOException {
        return new ArtifactoryRepo(uri, repoName, group, cacheFolder, RepoType.RELEASE);
    }

    private static ArtifactoryItem readItem(String url) throws IOException {
        try (Reader reader = new InputStreamReader(new URL(url).openStream(), StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, ArtifactoryItem.class);
        }
    }

    private static boolean matches(String uri) {
        if (uri.endsWith(".jar")) {
            return !uri.endsWith("-sources.jar") && !uri.endsWith("-javadoc.jar");
        }
        return false;
    }

    private ArtifactoryModule loadModuleFromCache(String moduleName) throws IOException {
        File cacheFile = getCacheFile(moduleName);

        if (cacheFile.exists()) {
            try (Reader reader = Files.newReader(cacheFile, StandardCharsets.UTF_8)) {
                return GSON.fromJson(reader, ArtifactoryModule.class);
            } catch (RuntimeException e) {
                logger.warn("Could not read {}", cacheFile, e);
                cacheFile.delete();
            }
        }
        return null;
    }

    private File getCacheFile(String moduleName) {
        return cacheFolder.resolve(moduleName).resolve("artifactory.json").toFile();
    }

    @Override
    public String getName() {
        return repoName;
    }

    @Override
    public RepoType getType() {
        return type;
    }

    @Override
    public Set<String> getModuleNames() {
        return artifactInfo.keySet();
    }

    @Override
    public void updateModule(String moduleName) throws IOException {
        String moduleUrl = baseUrl + "/" + moduleName;

        try {
            ArtifactoryItem mavenMeta = readItem(moduleUrl + "/maven-metadata.xml");
            ArtifactoryModule module = loadModuleFromCache(moduleName);

            if (module == null) {
                module = new ArtifactoryModule();
                module.lastModified = new Date(0);
                module.items = new ArrayList<>();
            }
            if (mavenMeta.lastModified.after(module.lastModified)) {
                module.lastModified = mavenMeta.lastModified;
                module.items.clear();

                ArtifactoryItem moduleFolder = readItem(moduleUrl);
                for (ArtifactoryItem.Entry child2 : moduleFolder.children) {
                    if (child2.folder) {
                        String versionUrl = moduleUrl + child2.uri;
                        Set<ArtifactoryArtifactInfo> entries = findArtifacts(versionUrl);

                        artifactInfo.put(moduleName, entries);
                        module.items.addAll(entries);

                    }
                }
                logger.info("Updated {}", moduleName);
            } else {
                logger.debug("No updates for {}", moduleName);
            }
            artifactInfo.put(moduleName, module.items);

            File cacheFile = getCacheFile(moduleName);
            cacheFile.getParentFile().mkdirs();
            try (Writer writer = Files.newWriter(cacheFile, StandardCharsets.UTF_8)) {
                GSON.toJson(module, writer);
            }
        } catch (FileNotFoundException e) {
            logger.info("No entries for '{}.{}' in '{}'", group, moduleName, repoName);
        }
    }

    private Set<ArtifactoryArtifactInfo> findArtifacts(String versionUrl) throws IOException {
        Set<ArtifactoryArtifactInfo> hits = new HashSet<>();

        ArtifactoryItem versionFolder = readItem(versionUrl);
        for (ArtifactoryItem.Entry child3 : versionFolder.children) {
            if (matches(child3.uri)) {
                String artifactUrl = versionUrl + child3.uri;
                ArtifactoryItem artifact = readItem(artifactUrl);
                hits.add(new ArtifactoryArtifactInfo(artifact));
                logger.debug("Added {}", artifactUrl);
            }
        }
        return hits;
    }

    @Override
    public Collection<? extends ArtifactInfo> getModuleArtifacts(String moduleName) {
        return Collections.unmodifiableCollection(artifactInfo.getOrDefault(moduleName, Collections.emptySet()));
    }
}
