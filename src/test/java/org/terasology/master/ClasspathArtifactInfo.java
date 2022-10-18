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

import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;
import org.terasology.module.RemoteModuleExtension;
import org.terasology.web.model.artifactory.ArtifactInfo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class ClasspathArtifactInfo implements ArtifactInfo {

    private final ModuleMetadata meta;
    private final URL url;
    private final String artifactName;

    public ClasspathArtifactInfo(String cpUrl) throws IOException {
        ModuleMetadataJsonAdapter metadataAdapter = new ModuleMetadataJsonAdapter();

        artifactName = cpUrl.substring(cpUrl.lastIndexOf("/") + 1);

        for (RemoteModuleExtension ext : RemoteModuleExtension.values()) {
            metadataAdapter.registerExtension(ext.getKey(), ext.getValueType());
        }

        url = getClass().getResource(cpUrl);
        try (Reader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
            meta = metadataAdapter.read(reader);
        }
    }

    @Override
    public URL getDownloadUrl() {
        return url;
    }

    @Override
    public String getArtifact() {
        return artifactName;
    }

    @Override
    public Date getLastUpdated() {
        return RemoteModuleExtension.getLastUpdated(meta);
    }

    @Override
    public long getFileSize() {
        return RemoteModuleExtension.getArtifactSize(meta);
    }

}
