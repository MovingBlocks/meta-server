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
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 *
 * @author Martin Steiger
 */
public class Downloader {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {

        URL url = new URL("http://master-server.herokuapp.com/servers/list");
        Charset cs = StandardCharsets.UTF_8;

        @SuppressWarnings({"serial"})
        Type entryListType = new TypeToken<List<ServerEntry>>() { /**/ }.getType();

        try (Reader reader = new InputStreamReader(url.openStream(), cs)) {
            List<ServerEntry> list = gson.fromJson(reader, entryListType);
            System.out.println(Joiner.on("\n").join(list));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
