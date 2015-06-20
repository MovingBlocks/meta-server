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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.junit.Assert;
import org.junit.Test;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;

import com.google.common.io.ByteStreams;
import com.google.gson.stream.JsonReader;

public class ModuleUpdateTest extends WebServerBasedTests {

    private final ModuleMetadataJsonAdapter adapter = new ModuleMetadataJsonAdapter();

    @Test
    public void testUpdate() throws MalformedURLException, IOException {

        Assert.assertEquals(0, readJsonList("/modules/list/CommonWorld").size());

        addSnapshot("CommonWorld", "CommonWorld-0.1.2-20150419.030003-8.jar_info.json");
        ModuleMetadata snapshot012 = readFromClasspath("CommonWorld-0.1.2-20150419.030003-8.jar_info.json");

        // send update notification
        String classpathFile = "/jenkins/CommonWorld-jenkins-notification.json";
        int responseCode = postNotification(new URL(URL_BASE + "/modules/update"), classpathFile);

        Assert.assertEquals(Status.OK.getStatusCode(), responseCode);
        Assert.assertEquals(1, readJsonList("/modules/list/CommonWorld").size());
        Assert.assertEquals(snapshot012, readJsonList("/modules/list/CommonWorld").get(0));

        addSnapshot("CommonWorld", "CommonWorld-0.1.3-20150608.034751-1.jar_info.json");

        // send 2nd update notification
        responseCode = postNotification(new URL(URL_BASE + "/modules/update"), classpathFile);
        Assert.assertEquals(Status.OK.getStatusCode(), responseCode);

        Assert.assertEquals(2, readJsonList("/modules/list/CommonWorld").size());

//        ModuleMetadata snapshot013 = readFromClasspath("CommonWorld-0.1.3-20150608.034751-1.jar_info.json");
//        Assert.assertEquals(snapshot013, readJsonList("/modules/list/CommonWorld").get(0));
    }

    private ModuleMetadata readFromClasspath(String path) throws IOException {
        URL url = getClass().getResource("/metas/" + path);
        try (Reader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
            return adapter.read(reader);
        }
    }

    private List<ModuleMetadata> readJsonList(String path) throws IOException {
        URL url = new URL(URL_BASE + path);

        List<ModuleMetadata> result = new ArrayList<>();
        try (Reader in = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
             JsonReader reader = new JsonReader(in)) {
            reader.beginArray();

            while (reader.hasNext()) {
                ModuleMetadata meta = adapter.read(reader);
                result.add(meta);
            }

            reader.endArray();
        }
        return result;
    }

    private int postNotification(URL url, String classpathFile) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // or manually with curl:
        // curl -X POST -d @<classpathFile> <url> --header "Content-Type:application/json"
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.connect();
        try (OutputStream output = connection.getOutputStream();
             InputStream input = getClass().getResourceAsStream(classpathFile)) {
            ByteStreams.copy(input, output);
        }

        return connection.getResponseCode();
    }
}
