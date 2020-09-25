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

import com.google.common.io.Files;
import io.micronaut.context.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.module.Module;
import org.terasology.module.*;
import org.terasology.naming.Name;
import org.terasology.naming.Version;
import org.terasology.web.artifactory.ArtifactInfo;
import org.terasology.web.artifactory.ArtifactRepository;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Provides a list of modules.
 */
@ThreadSafe
@Singleton
public class ModuleListModelImpl implements ModuleListModel {

    private static final Logger logger = LoggerFactory.getLogger(ModuleListModelImpl.class);

    private final ModuleMetadataJsonAdapter metadataAdapter = new ModuleMetadataJsonAdapter();

    private final ModuleRegistry moduleRegistry = new TableModuleRegistry();
    private final DependencyResolver dependencyResolver = new DependencyResolver(moduleRegistry);
    private final MetadataExtractor extractor;

    private final Collection<ArtifactRepository> repositories = new CopyOnWriteArrayList<>();

    private final Path cacheFolder;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    public ModuleListModelImpl(
            @Value("${meta-server.cacheFolder}") Path cacheFolder,
            MetadataExtractor extractor) {
        this.cacheFolder = cacheFolder;
        this.cacheFolder.toFile().mkdirs();
        this.extractor = extractor;

        for (RemoteModuleExtension ext : RemoteModuleExtension.values()) {
            metadataAdapter.registerExtension(ext.getKey(), ext.getValueType());
        }
    }

    public void addRepository(ArtifactRepository repo) {
        repositories.add(repo);
    }

    @Override
    public void updateAllModules() {
        for (ArtifactRepository repo : repositories) {
            for (String moduleName : repo.getModuleNames()) {
                try {
                    repo.updateModule(moduleName.toString());
                    updateModule(repo, moduleName);
                } catch (IOException e) {
                    logger.warn("Could not update module {}", moduleName);
                }
            }
        }
    }

    @Override
    public void updateModule(Name moduleName) {

        lock.writeLock().lock();
        try {
            moduleRegistry.removeIf(mod -> mod.getId().equals(moduleName));
        } finally {
            lock.writeLock().unlock();
        }

        String module = moduleName.toString();
        for (ArtifactRepository repo : repositories) {
            try {
                repo.updateModule(module);
                updateModule(repo, module);
            } catch (IOException e) {
                logger.warn("Could not update module {}", moduleName, e);
            }
        }
    }

    private void updateModule(ArtifactRepository repo, String moduleName) {
        lock.writeLock().lock();

        try {
            Path repoCacheFolder = cacheFolder.resolve(repo.getName());
            List<ModuleMetadata> releases = retrieveMetadata(repo, repoCacheFolder, moduleName);
            for (ModuleMetadata meta : releases) {
                switch (repo.getType()) {
                    case RELEASE:
                        if (!moduleRegistry.add(new RemoteModule(meta))) {
                            logger.error("Duplicate entry for {}/{}", meta.getId(), meta.getVersion());
                        }
                        break;

                    case SNAPSHOT:
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
                        break;

                    default:
                        logger.warn("Ignoring unknown repository type!");
                        break;
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private List<ModuleMetadata> retrieveMetadata(ArtifactRepository repository, Path repoCacheFolder, String moduleName) {
        Path moduleCacheFolder = repoCacheFolder.resolve(moduleName);
        moduleCacheFolder.toFile().mkdirs();

        List<ModuleMetadata> result = new ArrayList<>();

        logger.debug("Checking " + moduleName);

        Set<String> usedCacheFiles = new HashSet<>();
        for (ArtifactInfo info : repository.getModuleArtifacts(moduleName)) {
            try {
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
            } catch (IOException e) {
                logger.warn("Failed to parse info for '{}'", info, e);
            }
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
        lock.readLock().lock();
        try {
            return new HashSet<>(moduleRegistry.getModuleIds());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Collection<Module> getModuleVersions(Name module) {
        lock.readLock().lock();
        try {
            return new ArrayList<>(moduleRegistry.getModuleVersions(module));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Module getModule(Name module, Version version) {
        lock.readLock().lock();
        try {
            return moduleRegistry.getModule(module, version);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Module getLatestModuleVersion(Name name) {
        lock.readLock().lock();
        try {
            return moduleRegistry.getLatestModuleVersion(name);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Set<Module> resolve(Name name, Version version) {
        lock.readLock().lock();
        try {
            ResolutionResult result = dependencyResolver.builder().requireVersion(name, version).build();

            if (result.isSuccess()) {
                return result.getModules();
            } else {
                return Collections.emptySet();
            }
        } finally {
            lock.readLock().unlock();
        }

    }
}
