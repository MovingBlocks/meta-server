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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.Response.Status;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.ByteStreams;

public class ModuleTest extends WebServerBasedTests {

    @Test
    public void testUpdate() throws MalformedURLException, IOException {
        String classpathFile = "/CommonWorld-0.1.3-SNAPSHOT_jenkins-notification.json";
        int responseCode = postNotification(new URL(URL_BASE + "/modules/update"), classpathFile);

        Assert.assertEquals(Status.OK.getStatusCode(), responseCode);
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
