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
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;
import com.jcabi.w3c.Defect;
import com.jcabi.w3c.ValidationResponse;
import com.jcabi.w3c.Validator;
import com.jcabi.w3c.ValidatorBuilder;

/**
 * Use the w3c validator to verify that correct html code is generated.
 */
public class ValidatorTests extends WebServerBasedTests {

    private static final Logger logger = LoggerFactory.getLogger(ValidatorTests.class);

    private static Validator validator;

    @BeforeClass
    public static void createValidator() {
        validator = new ValidatorBuilder().html();
    }

    @Test
    public void testShowModuleListPage() throws IOException {
        analyzePage(new URL(URL_BASE + "/modules/show"));
    }

    @Test
    public void testShowModuleInfoPage() throws IOException {
        analyzePage(new URL(URL_BASE + "/modules/show/Core/0.53.1"));
    }

    @Test
    public void testShowServerListPage() throws IOException {
        analyzePage(new URL(URL_BASE + "/servers/show"));
    }

    @Test
    public void testShowAboutPage() throws IOException {
        analyzePage(new URL(URL_BASE + "/home"));
    }

    @Test
    public void testShowAddServerPage() throws IOException {
        analyzePage(new URL(URL_BASE + "/servers/add"));
    }

    private void analyzePage(URL url) throws IOException {
        try (InputStream is = url.openStream()) {
            InputStreamReader inr = new InputStreamReader(is, StandardCharsets.UTF_8);
            String text = CharStreams.toString(inr);
            ValidationResponse response = validator.validate(text);
            if (!response.valid()) {
                for (Defect error : response.warnings()) {
                    logger.warn(error.toString());
                }
                for (Defect error : response.errors()) {
                    logger.error("ERROR: " + error.toString());
                }
                Assert.fail();
            }
        }
    }
}
