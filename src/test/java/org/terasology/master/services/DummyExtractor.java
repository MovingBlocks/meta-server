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

package org.terasology.master.services;

import io.micronaut.context.annotation.Replaces;
import jakarta.inject.Singleton;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;
import org.terasology.module.RemoteModuleExtension;
import org.terasology.web.services.api.MetadataExtractor;
import org.terasology.web.services.impl.ZipExtractor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
 * Reads metadata from metadata files as-is.
 */
@Replaces(ZipExtractor.class)
@Singleton
public class DummyExtractor implements MetadataExtractor {

    private final ModuleMetadataJsonAdapter metaReader = new ModuleMetadataJsonAdapter();

    public DummyExtractor() {
        for (RemoteModuleExtension ext : RemoteModuleExtension.values()) {
            metaReader.registerExtension(ext.getKey(), ext.getValueType());
        }
    }

    @Override
    public ModuleMetadata loadMetaData(URL url) throws IOException {
        try (Reader reader = new InputStreamReader(url.openStream())) {
            return metaReader.read(reader);
        }
    }
}

