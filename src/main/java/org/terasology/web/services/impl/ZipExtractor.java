// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.web.services.impl;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;
import org.terasology.web.services.api.MetadataExtractor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
    public ZipExtractor(@Value("${meta-server.module.metadataFileNames}") String... filenames) {
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
