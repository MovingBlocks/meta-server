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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.terasology.web.model.server.ServerEntry;
import org.terasology.web.services.impl.geo.dbip.DbIpGeoLocationService;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.List;


/**
 *
 */
class ServerJsonListTest extends BaseTests {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    DbIpGeoLocationService dbIpGeoLocationService;

    @Test
    void testJson() throws IOException {
        @SuppressWarnings("serial")
        Type entryListType = new TypeToken<List<ServerEntry>>() { /**/
        }.getType();

        try (Reader reader = new StringReader(client.toBlocking().retrieve(HttpRequest.GET("/servers/list")))) {
            List<ServerEntry> list = GSON.fromJson(reader, entryListType);
            ServerEntry entry = list.get(0);

            Assertions.assertEquals(firstEntry, entry);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
