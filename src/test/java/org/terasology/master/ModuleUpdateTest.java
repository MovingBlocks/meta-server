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

import com.google.gson.stream.JsonReader;
import io.micronaut.core.io.IOUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class ModuleUpdateTest extends BaseTests {

    private final ModuleMetadataJsonAdapter adapter = new ModuleMetadataJsonAdapter();

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testUpdate() throws IOException {

        Assertions.assertEquals(0, readJsonList("/modules/list/CommonWorld").size());

        snapshotRepo.addArtifact("CommonWorld", new ClasspathArtifactInfo("/metas/" + "CommonWorld-0.1.2-20150419.030003-8.jar_info.json"));
        ModuleMetadata snapshot012 = readFromClasspath("CommonWorld-0.1.2-20150419.030003-8.jar_info.json");

        // send update notification
        String classpathFile = "/jenkins/CommonWorld-jenkins-notification.json";
        HttpStatus responseCode = postNotification("/modules/update", classpathFile);

        Assertions.assertEquals(HttpStatus.OK, responseCode);
        Assertions.assertEquals(1, readJsonList("/modules/list/CommonWorld").size());
        Assertions.assertEquals(snapshot012, readJsonList("/modules/list/CommonWorld").get(0));

        snapshotRepo.addArtifact("CommonWorld", new ClasspathArtifactInfo("/metas/" + "CommonWorld-0.1.3-20150608.034751-1.jar_info.json"));

        // send 2nd update notification
        responseCode = postNotification("/modules/update", classpathFile);
        Assertions.assertEquals(HttpStatus.OK, responseCode);

        Assertions.assertEquals(2, readJsonList("/modules/list/CommonWorld").size());

        ModuleMetadata snapshot013 = readFromClasspath("CommonWorld-0.1.3-20150608.034751-1.jar_info.json");
        Assertions.assertEquals(snapshot013, readJsonList("/modules/list/CommonWorld").get(0));
    }

    private ModuleMetadata readFromClasspath(String path) throws IOException {
        URL url = getClass().getResource("/metas/" + path);
        try (Reader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
            return adapter.read(reader);
        }
    }

    private List<ModuleMetadata> readJsonList(String path) throws IOException {
        String response = client.toBlocking().retrieve(HttpRequest.GET(path));

        List<ModuleMetadata> result = new ArrayList<>();
        try (Reader in = new StringReader(response);
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

    private HttpStatus postNotification(String path, String classpathFile) throws IOException {
        // curl -X POST -d @<classpathFile> <url> --header "Content-Type:application/json"
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(classpathFile)))) {
            String notificationBody = IOUtils.readText(reader);
            HttpResponse<?> response = client.toBlocking().exchange(HttpRequest.POST(path, notificationBody));
            return response.getStatus();
        }
    }
}
