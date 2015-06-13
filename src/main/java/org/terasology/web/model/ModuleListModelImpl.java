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
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;
import org.terasology.module.ModuleRegistry;
import org.terasology.module.RemoteModuleExtension;
import org.terasology.module.ResolutionResult;
import org.terasology.module.TableModuleRegistry;
import org.terasology.naming.Name;
import org.terasology.naming.Version;
import org.terasology.web.artifactory.ArtifactoryRepo;
import org.terasology.web.artifactory.ArtifactInfo;

import com.google.common.io.Files;

/**
 * Provides a list of modules.
 */
public class ModuleListModelImpl implements ModuleListModel {

    private static final Logger logger = LoggerFactory.getLogger(ModuleListModelImpl.class);

    private final ModuleMetadataJsonAdapter metadataAdapter = new ModuleMetadataJsonAdapter();

    private final ModuleRegistry moduleRegistry = new TableModuleRegistry();
    private final DependencyResolver dependencyResolver = new DependencyResolver(moduleRegistry);

    public ModuleListModelImpl(String host, String releaseRepo, String snapshotRepo) throws IOException {
        Path cacheFolder = Paths.get("cache", "modules");
        cacheFolder.toFile().mkdirs();

        for (RemoteModuleExtension ext : RemoteModuleExtension.values()) {
            metadataAdapter.registerExtension(ext.getKey(), ext.getValueType());
        }

        List<ModuleMetadata> releases = retrieveMetadata(host, releaseRepo, cacheFolder);
        for (ModuleMetadata meta : releases) {
            if (!moduleRegistry.add(new RemoteModule(meta))) {
                logger.error("Duplicate entry for {}/{}", meta.getId(), meta.getVersion());
            }
        }

        List<ModuleMetadata> snapshots = retrieveMetadata(host, snapshotRepo, cacheFolder);
        for (ModuleMetadata meta : snapshots) {
            Module prev = moduleRegistry.getModule(meta.getId(), meta.getVersion());
            if (prev != null) {
                Date prevUpdated = RemoteModuleExtension.getLastUpdated(prev.getMetadata());
                Date thisUpdated = RemoteModuleExtension.getLastUpdated(meta);

                if (thisUpdated.after(prevUpdated)) {

                    // remove the old one first so the new one can be added
                    moduleRegistry.remove(prev);
                    moduleRegistry.add(new RemoteModule(meta));
                }
            } else {
                moduleRegistry.add(new RemoteModule(meta));
            }
        }
    }

    private List<ModuleMetadata> retrieveMetadata(String host, String repo, Path cacheFolderBase) throws IOException {
        ZipExtractor extractor = new ZipExtractor("module.txt");
        Path cacheFolder = cacheFolderBase.resolve(repo);
        cacheFolder.toFile().mkdirs();

        ArtifactoryRepo repository = new ArtifactoryRepo(host, repo, cacheFolderBase);
        List<ModuleMetadata> result = new ArrayList<>();

        Set<String> usedCacheFiles = new HashSet<>();
        for (ArtifactInfo info : repository.getModuleArtifacts()) {
            ModuleMetadata meta;
            File cacheFile = cacheFolder.resolve(info.getArtifact() + "_info.json").toFile();
            if (cacheFile.exists()) {
                try (Reader reader = Files.newReader(cacheFile, StandardCharsets.UTF_8)) {
                    meta = metadataAdapter.read(reader);
                }
            } else {
                meta = extractor.loadMetaData(info.getDownloadUrl());
                RemoteModuleExtension.setDownloadUrl(meta, info.getDownloadUrl());
                RemoteModuleExtension.setArtifactSize(meta, info.getFileSize());
                RemoteModuleExtension.setLastUpdated(meta, info.getLastUpdated());
                try (Writer writer = Files.newWriter(cacheFile, StandardCharsets.UTF_8)) {
                    metadataAdapter.write(meta, writer);
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
    public Set<Name> getModuleIds() {
        return moduleRegistry.getModuleIds();
    }

    @Override
    public Collection<Module> getModuleVersions(Name module) {
        return moduleRegistry.getModuleVersions(module);
    }

    @Override
    public Module getModule(Name module, Version version) {
        return moduleRegistry.getModule(module, version);
    }

    @Override
    public Module getLatestModuleVersion(Name name) {
        return moduleRegistry.getLatestModuleVersion(name);
    }

    @Override
    public Set<Module> resolve(Name name) {
        ResolutionResult result = dependencyResolver.resolve(name);
        if (result.isSuccess()) {
            return result.getModules();
        } else {
            return Collections.emptySet();
        }
    }
}
