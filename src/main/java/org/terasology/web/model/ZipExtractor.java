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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataReader;

/**
 * Extracts the module info file from a ZIP archive that is described by an URL.
 * The stream is closed as soon as the file has been read.
 */
public class ZipExtractor {

    private final String filename;
    private final ModuleMetadataReader metaReader = new ModuleMetadataReader();

    /**
     * @param filename the file to
     */
    public ZipExtractor(String filename) {
        this.filename = filename;
    }

    /**
     * @param url the URL that describes the archive
     * @return the module metadata
     * @throws IOException if the file is not found or reading from the archive failed
     */
    public ModuleMetadata loadMetaData(URL url) throws IOException {
        try (InputStream in = url.openStream();
                ZipInputStream zipStream = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.getName().matches(filename)) {
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
