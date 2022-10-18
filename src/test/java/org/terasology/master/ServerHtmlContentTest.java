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
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import jakarta.inject.Inject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 *
 */
public class ServerHtmlContentTest extends BaseTests {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    public void testShowHtml() {
        String response = client.toBlocking().retrieve(HttpRequest.GET("/servers/show"));
        Document doc = Jsoup.parse(response);

        Element table = doc.getElementById("server-list");
        Assertions.assertTrue(table.nodeName().equals("table"));
        Element tableBody = table.select("tbody").first();
        Element firstRow = tableBody.select("tr").first();
        Assertions.assertEquals(firstEntry.getName(), firstRow.getElementsByClass("server-name").first().text());
        Assertions.assertEquals(firstEntry.getOwner(), firstRow.getElementsByClass("server-owner").first().text());
        Assertions.assertEquals("" + firstEntry.getPort(), firstRow.getElementsByClass("server-port").first().text());
        Assertions.assertEquals(firstEntry.getAddress(), firstRow.getElementsByClass("server-address").first().text());
    }
}
