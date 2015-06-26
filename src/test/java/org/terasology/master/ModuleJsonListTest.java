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
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;
import org.terasology.naming.Name;
import org.terasology.naming.Version;

import com.google.gson.stream.JsonReader;


/**
 * Tests the json operations in ModuleServlet.
 */
public class ModuleJsonListTest extends WebServerBasedTests {

    private static final ModuleMetadataJsonAdapter META_READER = new ModuleMetadataJsonAdapter();

    private final Charset charset = StandardCharsets.UTF_8;

    @Test
    public void testFullList() throws IOException {

        URL url = new URL(URL_BASE + "/modules/list");

        try (JsonReader reader = new JsonReader(new InputStreamReader(url.openStream(), charset))) {
            reader.beginArray();

            while (reader.hasNext()) {
                ModuleMetadata meta = META_READER.read(reader);
                Assert.assertNotNull(meta.getId());
                Assert.assertNotNull(meta.getVersion());
            }

            reader.endArray();
        }
    }

    @Test
    public void testLatestList() throws IOException {

        URL url = new URL(URL_BASE + "/modules/list/latest");
        Set<Name> ids = new HashSet<>();

        try (JsonReader reader = new JsonReader(new InputStreamReader(url.openStream(), charset))) {
            reader.beginArray();

            while (reader.hasNext()) {
                ModuleMetadata meta = META_READER.read(reader);
                Assert.assertNotNull(meta.getId());
                Assert.assertNotNull(meta.getVersion());

                Assert.assertTrue("Only one latest version per module is possible", ids.add(meta.getId()));
            }

            reader.endArray();
        }
    }

    @Test
    public void testSingleModuleList() throws IOException {

        URL url = new URL(URL_BASE + "/modules/list/Core");

        try (JsonReader reader = new JsonReader(new InputStreamReader(url.openStream(), charset))) {
            reader.beginArray();

            while (reader.hasNext()) {
                ModuleMetadata meta = META_READER.read(reader);
                Assert.assertEquals(new Name("Core"), meta.getId());
            }

            reader.endArray();
        }
    }

    @Test
    public void testSingleModuleVersion() throws IOException {

        URL url = new URL(URL_BASE + "/modules/list/Core/0.53.1");

        try (Reader reader = new InputStreamReader(url.openStream(), charset)) {
            ModuleMetadata meta = META_READER.read(reader);
            Assert.assertEquals(new Name("Core"), meta.getId());
            Assert.assertEquals(new Version("0.53.1"), meta.getVersion());
        }
    }

    @Test
    public void testSingleModuleLatestVersion() throws IOException {

        URL url = new URL(URL_BASE + "/modules/list/Core/latest");

        try (Reader reader = new InputStreamReader(url.openStream(), charset)) {
            ModuleMetadata meta = META_READER.read(reader);
            Assert.assertEquals(new Name("Core"), meta.getId());
            Assert.assertEquals(new Version("0.53.1"), meta.getVersion());
        }
    }
}
