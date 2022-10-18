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

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 *
 */
@MicronautTest
class ModuleShowTest extends BaseTests {

    private final Charset charset = StandardCharsets.UTF_8;

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testNonExistingModuleVersion() {
        HttpClientResponseException exception = Assertions.assertThrows(HttpClientResponseException.class,
                () -> client.toBlocking().retrieve(HttpRequest.GET("/modules/show/Core/23.1337.23")),
                "Request to not existing moduel version should thrown an HttpClientResponseException"
        );
        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatus(), "Status must be 404");
    }


    @Test
    void testInvalidVersion() {
        HttpClientResponseException exception = Assertions.assertThrows(HttpClientResponseException.class,
                () -> client.toBlocking().retrieve(HttpRequest.GET("/modules/show/Core/sdfdsad")),
                "Request to unknown module should thrown an HttpClientResponseException"
        );
        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatus(), "Status must be 404");
    }

    @Test
    void testUnknownModuleLatestVersion() {
        HttpClientResponseException exception = Assertions.assertThrows(HttpClientResponseException.class,
                () -> client.toBlocking().retrieve(HttpRequest.GET("/modules/show/notThere/latest")),
                "Request to invalid version should thrown an HttpClientResponseException"
        );
        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatus(), "Status must be 404");
    }

    @Test
    void testUnknownModuleInvalidVersion() {
        HttpClientResponseException exception = Assertions.assertThrows(HttpClientResponseException.class,
                () -> client.toBlocking().retrieve(HttpRequest.GET("/modules/show/notThere/1.2.3")),
                "Request to unknown module should thrown an HttpClientResponseException"
        );
        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatus(), "Status must be 404");
    }
}
