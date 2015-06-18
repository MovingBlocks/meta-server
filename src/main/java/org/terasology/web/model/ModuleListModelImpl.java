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
import org.terasology.web.artifactory.ArtifactInfo;
import org.terasology.web.artifactory.ArtifactRepository;
import org.terasology.web.artifactory.ArtifactoryRepo;

import com.google.common.io.Files;

/**
 * Provides a list of modules.
 */
public class ModuleListModelImpl implements ModuleListModel {

    private static final Logger logger = LoggerFactory.getLogger(ModuleListModelImpl.class);

    private final ModuleMetadataJsonAdapter metadataAdapter = new ModuleMetadataJsonAdapter();

    private final ModuleRegistry moduleRegistry = new TableModuleRegistry();
    private final DependencyResolver dependencyResolver = new DependencyResolver(moduleRegistry);
    private final ZipExtractor extractor = new ZipExtractor("module.txt");

    private final ArtifactoryRepo releaseRepository;
    private final ArtifactoryRepo snapshotRepository;

    private final Path releaseRepoCacheFolder;
    private final Path snapshotRepoCacheFolder;

    public ModuleListModelImpl(String host, String releaseRepoName, String snapshotRepoName) throws IOException {

        Path cacheFolder = Paths.get("cache", "modules");
        cacheFolder.toFile().mkdirs();

        for (RemoteModuleExtension ext : RemoteModuleExtension.values()) {
            metadataAdapter.registerExtension(ext.getKey(), ext.getValueType());
        }

        releaseRepoCacheFolder = cacheFolder.resolve(releaseRepoName);
        releaseRepository = new ArtifactoryRepo(host, releaseRepoName, releaseRepoCacheFolder);
        for (String moduleName : releaseRepository.getModuleNames()) {
            updateReleaseModule(moduleName);
        }

        snapshotRepoCacheFolder = cacheFolder.resolve(snapshotRepoName);
        snapshotRepository = new ArtifactoryRepo(host, snapshotRepoName, snapshotRepoCacheFolder);
        for (String moduleName : snapshotRepository.getModuleNames()) {
            updateSnapshotModule(moduleName);
        }
    }

    private void updateReleaseModule(String moduleName) throws IOException {
        List<ModuleMetadata> releases = retrieveMetadata(releaseRepository, releaseRepoCacheFolder, moduleName);
        for (ModuleMetadata meta : releases) {
            if (!moduleRegistry.add(new RemoteModule(meta))) {
                logger.error("Duplicate entry for {}/{}", meta.getId(), meta.getVersion());
            }
        }
    }

    private void updateSnapshotModule(String moduleName) throws IOException {
        List<ModuleMetadata> snapshots = retrieveMetadata(snapshotRepository, snapshotRepoCacheFolder, moduleName);
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

    @Override
    public void updateModule(Name moduleName) {
        moduleRegistry.removeIf(mod -> mod.getId().equals(moduleName));

        try {
            updateReleaseModule(moduleName.toString());
            updateSnapshotModule(moduleName.toString());
        } catch (IOException e) {
            logger.warn("Could not update module {}", moduleName, e);
        }
    }

    private List<ModuleMetadata> retrieveMetadata(ArtifactRepository repository, Path cacheFolder, String moduleName) throws IOException {
        Path moduleCacheFolder = cacheFolder.resolve(moduleName);
        moduleCacheFolder.toFile().mkdirs();

        List<ModuleMetadata> result = new ArrayList<>();

        logger.debug("Checking " + moduleName);

        Set<String> usedCacheFiles = new HashSet<>();
        for (ArtifactInfo info : repository.getModuleArtifacts(moduleName)) {
            ModuleMetadata meta;
            File cacheFile = moduleCacheFolder.resolve(info.getArtifact() + "_info.json").toFile();
            if (cacheFile.exists()) {
                try (Reader reader = Files.newReader(cacheFile, StandardCharsets.UTF_8)) {
                    meta = metadataAdapter.read(reader);
                }
            } else {
                logger.debug("Downloading " + info.getDownloadUrl());

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

        for (String fname : moduleCacheFolder.toFile().list()) {
            if (fname.endsWith("_info.json") && !usedCacheFiles.contains(fname)) {
                logger.info("Would delete " + fname);
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
