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

package org.terasology.master;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

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
import org.terasology.web.model.ModuleListModel;
import org.terasology.web.model.RemoteModule;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class DummyModuleListModel implements ModuleListModel {

    private final ModuleRegistry moduleRegistry = new TableModuleRegistry();
    private final DependencyResolver dependencyResolver = new DependencyResolver(moduleRegistry);
    private final ModuleMetadataJsonAdapter metadataAdapter = new ModuleMetadataJsonAdapter();

    public DummyModuleListModel() throws IOException {

        for (RemoteModuleExtension ext : RemoteModuleExtension.values()) {
            metadataAdapter.registerExtension(ext.getKey(), ext.getValueType());
        }

        moduleRegistry.add(load("/Core-0.53.1.jar_info.json"));
        moduleRegistry.add(load("/MusicDirector-0.2.1-20150608.041945-1.jar_info.json"));
        moduleRegistry.add(load("/ChrisVolume1OST-0.2.1-20150608.034649-1.jar_info.json"));
    }

    private Module load(String cpUrl) throws IOException {
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream(cpUrl), StandardCharsets.UTF_8)) {
            ModuleMetadata meta = metadataAdapter.read(reader);
            return new RemoteModule(meta);
        }
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
