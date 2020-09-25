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

import org.terasology.web.model.server.ServerEntry;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Describes a permanent data storage.
 */
public interface DatabaseService {

    /**
     * @param tableName the name of the DB table
     * @param address   the server address
     * @param port      the server port
     * @return true if the entry was found and removed, false otherwise
     * @throws SQLException if the table query fails
     */
    boolean remove(String tableName, String address, int port) throws SQLException;

    /**
     * Retrieves the contents of a table
     *
     * @param tableName the name of the table in the DB
     * @return a list of rows
     * @throws SQLException if the table query fails
     */
    List<Map<String, Object>> readAll(String tableName) throws SQLException;

    boolean insert(String tableName, String name, String address, int port, String owner, boolean active) throws SQLException;

    boolean update(String tableName, String name, String address, int port, String owner, boolean active) throws SQLException;

    void createTable(String tableName) throws SQLException;

    default void insert(String tableName, ServerEntry entry) throws SQLException {
        insert(tableName, entry.getName(), entry.getAddress(), entry.getPort(), entry.getOwner(), entry.isActive());
    }
}
