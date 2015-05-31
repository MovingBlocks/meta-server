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

package org.terasology.web.model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.terasology.web.model.ModuleListModelImpl.SearchResult.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Provides a list of modules.
 */
public class ModuleListModelImpl implements ModuleListModel {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public List<ModuleInfo> findModules() throws IOException {
        URL url = new URL("http://artifactory.terasology.org/artifactory/api/search/gavc" +
                "?g=org.terasology.modules&repos=terasology-release-local,terasology-snapshot-local");

        Charset cs = StandardCharsets.UTF_8;
        List<ModuleInfo> uris = new ArrayList<>();
        try (Reader reader = new InputStreamReader(url.openStream(), cs)) {
            SearchResult result = GSON.fromJson(reader, SearchResult.class);
            for (Entry entry : result.results) {
                String uri = entry.uri;
                if (uri.endsWith(".jar")) {
                    if (!uri.endsWith("-sources.jar") && !uri.endsWith("-javadoc.jar")) {
                        String dlUri = uri.replace("/api/storage", "");
                        ModuleInfo mod = new ModuleInfo(dlUri);
                        uris.add(mod);
                    }
                }
            }
        }

        return uris;
    }

    class SearchResult {
        List<Entry> results;

        class Entry {
            String uri;
        }
    }
}
