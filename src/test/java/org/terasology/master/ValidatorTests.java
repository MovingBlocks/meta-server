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

import com.jcabi.w3c.Defect;
import com.jcabi.w3c.ValidationResponse;
import com.jcabi.w3c.Validator;
import com.jcabi.w3c.ValidatorBuilder;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Use the w3c validator to verify that correct html code is generated.
 */

class ValidatorTests extends BaseTests {

    private static final Logger logger = LoggerFactory.getLogger(ValidatorTests.class);

    private static final Validator validator = new ValidatorBuilder().html();

    @Inject
    @Client("/")
    HttpClient client;

    @ParameterizedTest()
    @ValueSource(strings = {
            "/modules/show",
            "/modules/show/Core",
            "/modules/show/Core/0.53.1",
            "/servers/show",
            "/home",
            "/servers/add",
            "/servers/edit?index=0",
    })
    void testW3CValidation(String uri) throws IOException {
        analyzePage(uri);
    }

    private void analyzePage(String uri) throws IOException {
        String text = client.toBlocking().retrieve(HttpRequest.GET(uri));
        ValidationResponse response = validator.validate(text);
        if (!response.valid()) {
            for (Defect error : response.warnings()) {
                logger.warn(error.toString());
            }
            for (Defect error : response.errors()) {
                logger.error("ERROR: " + error.toString());
            }
            Assertions.fail("W3C Validation failed, see logs");
        }
    }
}
