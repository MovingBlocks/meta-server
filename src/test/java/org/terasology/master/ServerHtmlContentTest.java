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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 */
public class ServerHtmlContentTest extends WebServerBasedTests {

    @Test
    public void testShowHtml() throws IOException {
        String url = URL_BASE + "/servers/show";

        Document doc = Jsoup.connect(url).get();

        Element table = doc.getElementById("server-list");
        Assert.assertTrue(table.nodeName().equals("table"));
        Element tableBody = table.select("tbody").first();
        Element firstRow = tableBody.select("tr").first();
        Assert.assertEquals(firstEntry.getName(), firstRow.getElementsByClass("server-name").first().text());
        Assert.assertEquals(firstEntry.getOwner(), firstRow.getElementsByClass("server-owner").first().text());
        Assert.assertEquals("" + firstEntry.getPort(), firstRow.getElementsByClass("server-port").first().text());
        Assert.assertEquals(firstEntry.getAddress(), firstRow.getElementsByClass("server-address").first().text());
    }
}
