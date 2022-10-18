// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.web.services.impl;

import com.google.common.io.Files;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.module.Module;
import org.terasology.naming.Name;
import org.terasology.naming.Version;
import org.terasology.web.model.artifactory.ArtifactInfo;
import org.terasology.web.model.artifactory.ArtifactRepository;
import org.terasology.web.model.module.RemoteModule;
import org.terasology.web.services.api.MetadataExtractor;
import org.terasology.web.services.api.ModuleListService;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.terasology.module.DependencyResolver;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;
import org.terasology.module.ModuleRegistry;
import org.terasology.module.RemoteModuleExtension;
import org.terasology.module.ResolutionResult;
import org.terasology.module.TableModuleRegistry;

import com.google.common.collect.ImmutableList;

/**
 * Provides a list of modules.
 */
@ThreadSafe
@Singleton
public class ModuleListServiceImpl implements ModuleListService {

    private static final Logger logger = LoggerFactory.getLogger(ModuleListServiceImpl.class);

    private final ModuleMetadataJsonAdapter metadataAdapter = new ModuleMetadataJsonAdapter();

    private final ModuleRegistry moduleRegistry = new TableModuleRegistry();
    private final DependencyResolver dependencyResolver = new DependencyResolver(moduleRegistry);
    private final MetadataExtractor extractor;

    private final Collection<ArtifactRepository> repositories = new CopyOnWriteArrayList<>();

    private final Path cacheFolder;

    private final List<String> ignoredModules = ImmutableList.of("engine", "engine-tests");

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    public ModuleListServiceImpl(@Value("${meta-server.cache.folder}") Path cacheFolder, Collection<ArtifactRepository> repositories, MetadataExtractor extractor) {
        this.cacheFolder = cacheFolder;
        this.cacheFolder.toFile().mkdirs();
        this.extractor = extractor;
        this.repositories.addAll(repositories);

        for (RemoteModuleExtension ext : RemoteModuleExtension.values()) {
            metadataAdapter.registerExtension(ext.getKey(), ext.getValueType());
        }
    }

    @Deprecated
    public void addRepository(ArtifactRepository repo) {
        repositories.add(repo);
    }

    @Override
    public void updateAllModules() {
        for (ArtifactRepository repo : repositories) {
            for (String moduleName : repo.getModuleNames()) {
                if (isRelevant(moduleName)) {
                    try {
                        repo.updateModule(moduleName);
                        updateModule(repo, moduleName);
                    } catch (IOException e) {
                        logger.warn("Could not update module {}", moduleName);
                    }
                }
            }
        }
    }

    private boolean isRelevant(String moduleName) {
        return !ignoredModules.contains(moduleName);
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

        logger.debug("Checking {}", moduleName);

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
                    logger.debug("Downloading {}", info.getDownloadUrl());

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
                logger.info("Would delete {}", fname);
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
