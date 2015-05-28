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
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.web.DataBase;
import org.terasology.web.JettyMain;
import org.terasology.web.JooqDatabase;
import org.terasology.web.model.ServerEntry;
import org.terasology.web.model.ServerListModel;
import org.terasology.web.model.ServerListModelImpl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 *
 * @author Martin Steiger
 */
public class ServerJsonListTest {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final int PORT = 8082;
    private static final String URL_BASE = "http://localhost:" + PORT;

    private static final String DB_URL = "jdbc:h2:mem:test";

    private static Server server;

    @BeforeClass
    public static void setup() throws Exception {

        String secret = "edit";

        DataBase dataBase = new JooqDatabase(DB_URL);

        ServerListModel serverListModel = new ServerListModelImpl(dataBase, "servers", secret);

        server = JettyMain.start(PORT, serverListModel);
    }

    @AfterClass
    public static void shutdown() throws Exception {
        server.stop();
    }

    @Test
    public void testJson() throws MalformedURLException {
        URL url = new URL(URL_BASE + "/servers/list");
        Charset cs = StandardCharsets.UTF_8;

        @SuppressWarnings("serial")
        Type entryListType = new TypeToken<List<ServerEntry>>() { /**/ }.getType();

        try (Reader reader = new InputStreamReader(url.openStream(), cs)) {
            List<ServerEntry> list = GSON.fromJson(reader, entryListType);
            Assert.assertNotNull(list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
