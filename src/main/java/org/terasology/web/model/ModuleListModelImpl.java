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

package org.terasology.web.model;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;
import org.terasology.module.RemoteModuleExtension;
import org.terasology.naming.Name;
import org.terasology.naming.Version;
import org.terasology.web.artifactory.ArtifactoryRepo;
import org.terasology.web.artifactory.ModuleInfo;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.io.Files;

/**
 * Provides a list of modules.
 */
public class ModuleListModelImpl implements ModuleListModel {

    private static final Logger logger = LoggerFactory.getLogger(ModuleListModelImpl.class);

    private final ModuleMetadataJsonAdapter metadataIO = new ModuleMetadataJsonAdapter();
    private final Table<Name, Version, List<ModuleMetadata>> moduleMetas = HashBasedTable.create();

    public ModuleListModelImpl() throws IOException {
        String host = "http://artifactory.terasology.org/artifactory";
        Path cacheFolder = Paths.get("cache", "modules");
        cacheFolder.toFile().mkdirs();

        for (RemoteModuleExtension ext : RemoteModuleExtension.values()) {
            metadataIO.registerExtension(ext.getKey(), ext.getValueType());
        }

        List<ModuleMetadata> releases = retrieveMetadata(host, "terasology-release-local", cacheFolder);
        for (ModuleMetadata meta : releases) {
            List<ModuleMetadata> list = moduleMetas.get(meta.getId(), meta.getVersion());
            if (list != null) {
                logger.error("Duplicate entry for {}/{}", meta.getId(), meta.getVersion());
            }
            moduleMetas.put(meta.getId(), meta.getVersion(), Collections.singletonList(meta));
        }

        List<ModuleMetadata> snapshots = retrieveMetadata(host, "terasology-snapshot-local", cacheFolder);
        for (ModuleMetadata meta : snapshots) {
            Version ov = meta.getVersion();
            Version snapshotVersion = new Version(ov.getMajor(), ov.getMinor(), ov.getPatch(), true);
            List<ModuleMetadata> list = moduleMetas.get(meta.getId(), snapshotVersion);
            if (list == null) {
                list = new ArrayList<>();
                moduleMetas.put(meta.getId(), snapshotVersion, list);
            }
            list.add(meta);
        }

    }

    private List<ModuleMetadata> retrieveMetadata(String host, String repo, Path cacheFolderBase) throws IOException {
        ZipExtractor extractor = new ZipExtractor("module.txt");
        Path cacheFolder = cacheFolderBase.resolve(repo);
        cacheFolder.toFile().mkdirs();

        ArtifactoryRepo repository = new ArtifactoryRepo(host, repo, cacheFolderBase);
        List<ModuleMetadata> result = new ArrayList<>();

        Set<String> usedCacheFiles = new HashSet<>();
        for (ModuleInfo info : repository.getModules()) {
            ModuleMetadata meta;
            File cacheFile = cacheFolder.resolve(info.getArtifact() + "_info.json").toFile();
            if (cacheFile.exists()) {
                try (Reader reader = Files.newReader(cacheFile, StandardCharsets.UTF_8)) {
                    meta = metadataIO.read(reader);
                }
            } else {
                meta = extractor.loadMetaData(info.getDownloadUrl());
                RemoteModuleExtension.setDownloadUrl(meta, info.getDownloadUrl());
                RemoteModuleExtension.setArtifactSize(meta, info.getFileSize());
                RemoteModuleExtension.setLastUpdated(meta, info.getLastUpdated());
                try (Writer writer = Files.newWriter(cacheFile, StandardCharsets.UTF_8)) {
                    metadataIO.write(meta, writer);
                }
            }
            usedCacheFiles.add(cacheFile.getName());
            result.add(meta);
        }

        for (String fname : cacheFolder.toFile().list()) {
            if (!usedCacheFiles.contains(fname)) {
                System.out.println("ModuleListModelImpl: Would delete " + fname);
            }
        }
        return result;
    }

    @Override
    public Set<Name> findModules() {
        return moduleMetas.rowKeySet();
    }

    @Override
    public Set<Version> findVersions(Name module) {
        return moduleMetas.row(module).keySet();
    }

    @Override
    public List<ModuleMetadata> findMetadata(Name module, Version version) {
        return moduleMetas.get(module, version);
    }
}
