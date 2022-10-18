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

import org.terasology.web.model.server.Result;
import org.terasology.web.model.server.ServerEntry;

import java.io.IOException;
import java.util.List;

public interface ServerListService {

    List<ServerEntry> getServers() throws IOException;

    Result addServer(String name, String address, int port, String owner, boolean active, String secret);

    Result updateServer(String name, String address, int port, String owner, boolean active, String secret);

    Result removeServer(String address, int port, String secret);

}
