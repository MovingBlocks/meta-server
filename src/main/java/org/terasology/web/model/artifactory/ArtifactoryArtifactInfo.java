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

package org.terasology.web.model.artifactory;

import com.google.common.base.Preconditions;

import java.net.URL;
import java.util.Date;

/**
 * Describes a module based on its URL in Artifactory.
 */
public class ArtifactoryArtifactInfo implements ArtifactInfo {
    private final String artifact;
    private final ArtifactoryItem item;

    public ArtifactoryArtifactInfo(ArtifactoryItem item) {
        Preconditions.checkArgument(item != null);

        this.item = item;
        String[] parts = item.downloadUri.getPath().split("/");
        int count = parts.length;
        artifact = parts[count - 1];
    }

    @Override
    public URL getDownloadUrl() {
        return item.downloadUri;
    }

    @Override
    public String getArtifact() {
        return artifact;
    }

    @Override
    public Date getLastUpdated() {
        return item.lastUpdated;
    }

    @Override
    public long getFileSize() {
        return item.size;
    }
}
