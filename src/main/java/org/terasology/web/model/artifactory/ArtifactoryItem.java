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

package org.terasology.web.model.artifactory;

import java.net.URL;
import java.util.Date;
import java.util.List;

class ArtifactoryItem {
    String repo;                  // "terasology-release-local",
    String path;                  // "/org",
    String created;               // "2014-11-10T00:57:29.124-05:00",
    String createdBy;             // "gooey",
    Date lastModified;            // "2014-11-10T00:57:29.124-05:00",
    String modifiedBy;            // "gooey",
    Date lastUpdated;             // "2014-11-10T00:57:29.124-05:00",
    List<Entry> children;
    String uri;                   // "http://artifactory.terasology.org/artifactory/api/storage/terasology-release-local/org"
    URL downloadUri;              // "http://a.t.o/a/t-s-l/o/t/m/BlockPicker/0.1.0-SNAPSHOT/BP-0.1.0-20150124.022632-3.jar",
    String mimeType;              // "application/java-archive",
    int size;                     // "14063",

    static class Entry {
        String uri;             // "/terasology",
        boolean folder;
    }
}

