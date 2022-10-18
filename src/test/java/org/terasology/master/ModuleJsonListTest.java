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
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;
import org.terasology.naming.Name;
import org.terasology.naming.Version;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;


/**
 * Tests the json operations in ModuleServlet.
 */
@MicronautTest
class ModuleJsonListTest extends BaseTests {

    private static final ModuleMetadataJsonAdapter META_READER = new ModuleMetadataJsonAdapter();

    private final Charset charset = StandardCharsets.UTF_8;

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testFullList() throws IOException {
        String response = client.toBlocking().retrieve(HttpRequest.GET("/modules/list"));

        try (JsonReader reader = new JsonReader(new StringReader(response))) {
            reader.beginArray();

            while (reader.hasNext()) {
                ModuleMetadata meta = META_READER.read(reader);
                Assertions.assertNotNull(meta.getId());
                Assertions.assertNotNull(meta.getVersion());
            }

            reader.endArray();
        }
    }

    @Test
    void testLatestList() throws IOException {

        String response = client.toBlocking().retrieve(HttpRequest.GET("/modules/list/latest"));
        Set<Name> ids = new HashSet<>();

        try (JsonReader reader = new JsonReader(new StringReader(response))) {
            reader.beginArray();

            while (reader.hasNext()) {
                ModuleMetadata meta = META_READER.read(reader);
                Assertions.assertAll(
                        () -> Assertions.assertNotNull(meta.getId(), "Id must be provided"),
                        () -> Assertions.assertNotNull(meta.getVersion(), "Version must be provided"),

                        () -> Assertions.assertTrue(ids.add(meta.getId()), "Only one latest version per module is possible")
                );
            }

            reader.endArray();
        }
    }

    @Test
    void testSingleModuleList() throws IOException {

        String response = client.toBlocking().retrieve(HttpRequest.GET("/modules/list/Core"));

        try (JsonReader reader = new JsonReader(new StringReader(response))) {
            reader.beginArray();

            while (reader.hasNext()) {
                ModuleMetadata meta = META_READER.read(reader);
                Assertions.assertEquals(new Name("Core"), meta.getId());
            }

            reader.endArray();
        }
    }

    @Test
    void testSingleModuleVersion() throws IOException {

        String response = client.toBlocking().retrieve(HttpRequest.GET("/modules/list/Core/0.53.1"));

        try (Reader reader = new StringReader(response)) {
            ModuleMetadata meta = META_READER.read(reader);
            Assertions.assertEquals(new Name("Core"), meta.getId());
            Assertions.assertEquals(new Version("0.53.1"), meta.getVersion());
        }
    }

    @Test
    void testNonExistingModuleVersion() throws IOException {
        HttpClientResponseException exception = Assertions.assertThrows(HttpClientResponseException.class,
                () -> client.toBlocking().retrieve(HttpRequest.GET("/modules/list/Core/23.1337.23")),
                "Request to Not existiong module version should thrown an HttpClientResponseException"
        );
        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatus(), "Status must be 404");
    }

    @Test
    void testInvalidVersion() throws IOException {
        HttpClientResponseException exception = Assertions.assertThrows(HttpClientResponseException.class,
                () -> client.toBlocking().retrieve(HttpRequest.GET("/modules/list/Core/asdfd")),
                "Request to invalid version should thrown an HttpClientResponseException"
        );
        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatus(), "Status must be 404");
    }

    @Test
    void testUnknownModuleLatestVersion() throws IOException {
        HttpClientResponseException exception = Assertions.assertThrows(HttpClientResponseException.class,
                () -> client.toBlocking().retrieve(HttpRequest.GET("/modules/list/notThere/latest")),
                "Request to Unknown Module Version should thrown an HttpClientResponseException"
        );
        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatus(), "Status must be 404");
    }

    @Test
    void testUnknownModuleInvalidVersion() throws IOException {
        HttpClientResponseException exception = Assertions.assertThrows(HttpClientResponseException.class,
                () -> client.toBlocking().retrieve(HttpRequest.GET("/modules/list/notThere/1.2.3")),
                "Request to unknown module should thrown an HttpClientResponseException"
        );
        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatus(), "Status must be 404");
    }

    @Test
    void testSingleModuleLatestVersion() throws IOException {

        String response = client.toBlocking().retrieve(HttpRequest.GET("/modules/list/Core/latest"));

        try (Reader reader = new StringReader(response)) {
            ModuleMetadata meta = META_READER.read(reader);
            Assertions.assertEquals(new Name("Core"), meta.getId());
            Assertions.assertEquals(new Version("0.53.1"), meta.getVersion());
        }
    }
}
