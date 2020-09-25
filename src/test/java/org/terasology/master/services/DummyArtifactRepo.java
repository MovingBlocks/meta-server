/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.master.services;

import org.terasology.web.model.artifactory.ArtifactInfo;
import org.terasology.web.model.artifactory.ArtifactRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DummyArtifactRepo implements ArtifactRepository {

    private final RepoType type;
    private final Map<String, Collection<ArtifactInfo>> modules = new LinkedHashMap<>();

    public DummyArtifactRepo(RepoType type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return "dummyRepo";
    }

    @Override
    public RepoType getType() {
        return type;
    }

    public void addArtifact(String moduleName, ArtifactInfo info) {
        modules.computeIfAbsent(moduleName, k -> new ArrayList<>()).add(info);
    }

    @Override
    public Collection<String> getModuleNames() {
        return Collections.unmodifiableCollection(modules.keySet());
    }

    @Override
    public void updateModule(String moduleName) throws IOException {
        // ignore
    }

    @Override
    public Collection<ArtifactInfo> getModuleArtifacts(String moduleName) {
        return Collections.unmodifiableCollection(modules.getOrDefault(moduleName, Collections.emptySet()));
    }

}
