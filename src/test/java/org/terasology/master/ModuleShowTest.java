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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

/**
 *
 */
public class ModuleShowTest extends WebServerBasedTests {

    private final Charset charset = StandardCharsets.UTF_8;

    @Test(expected = FileNotFoundException.class)
    public void testNonExistingModuleVersion() throws IOException {

        URL url = new URL(URL_BASE + "/modules/show/Core/23.1337.23");

        try (Reader reader = new InputStreamReader(url.openStream(), charset)) {
            reader.read();
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void testInvalidVersion() throws IOException {

        URL url = new URL(URL_BASE + "/modules/show/Core/sdfsdfs");

        try (Reader reader = new InputStreamReader(url.openStream(), charset)) {
            reader.read();
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void testUnknownModuleLatestVersion() throws IOException {

        URL url = new URL(URL_BASE + "/modules/show/notThere/latest");

        try (Reader reader = new InputStreamReader(url.openStream(), charset)) {
            reader.read();
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void testUnknownModuleInvalidVersion() throws IOException {

        URL url = new URL(URL_BASE + "/modules/show/notThere/1.2.3");

        try (Reader reader = new InputStreamReader(url.openStream(), charset)) {
            reader.read();
        }
    }
}
