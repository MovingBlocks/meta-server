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

package org.terasology.web.services.api;

import org.terasology.module.ModuleMetadata;

import java.io.IOException;
import java.net.URL;

public interface MetadataExtractor {

    /**
     * @param url the URL that describes the archive
     * @return the module metadata
     * @throws IOException if the file is not found or reading from the archive failed
     */
    ModuleMetadata loadMetaData(URL url) throws IOException;
}
