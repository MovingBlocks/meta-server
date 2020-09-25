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

package org.terasology.web.services.impl;

import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;
import org.terasology.web.services.api.MetadataExtractor;

import javax.inject.Singleton;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Extracts the module info file from a ZIP archive that is described by an URL.
 * The stream is closed as soon as the file has been read.
 */
@Singleton
public class ZipExtractor implements MetadataExtractor {

    private final List<String> filename;
    private final ModuleMetadataJsonAdapter metaReader = new ModuleMetadataJsonAdapter();

    /**
     * @param filenames the collection file names that match. The first match is returned.
     */
    public ZipExtractor(String... filenames) {
        this.filename = Arrays.asList(filenames);
    }

    @Override
    public ModuleMetadata loadMetaData(URL url) throws IOException {
        try (InputStream in = url.openStream();
                ZipInputStream zipStream = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (filename.contains(entry.getName())) {
                    return readFile(zipStream);
                }
            }
        }
        throw new FileNotFoundException("File '" + filename + "' not found in archive + '" + url + "'.");
    }

    private ModuleMetadata readFile(ZipInputStream stream) throws IOException {
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return metaReader.read(reader);
        }
    }

}
