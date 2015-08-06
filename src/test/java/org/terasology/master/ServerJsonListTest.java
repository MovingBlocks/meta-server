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
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.terasology.web.model.ServerEntry;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 *
 */
public class ServerJsonListTest extends WebServerBasedTests {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Test
    public void testJson() throws MalformedURLException {

        @SuppressWarnings("serial")
        Type entryListType = new TypeToken<List<ServerEntry>>() { /**/ }.getType();

        URL url = new URL(URL_BASE + "/servers/list");
        Charset cs = StandardCharsets.UTF_8;
        try (Reader reader = new InputStreamReader(url.openStream(), cs)) {
            List<ServerEntry> list = GSON.fromJson(reader, entryListType);
            ServerEntry entry = list.get(0);

            Assert.assertEquals(entry, firstEntry);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
