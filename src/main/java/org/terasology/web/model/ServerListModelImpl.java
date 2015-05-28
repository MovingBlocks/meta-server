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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.web.DataBase;

import com.google.common.base.Preconditions;

public class ServerListModelImpl implements ServerListModel {

    private static final Logger logger = LoggerFactory.getLogger(ServerListModelImpl.class);

    private final DataBase dataBase;
    private final String tableName;
    private final String editSecret;

    public ServerListModelImpl(DataBase dataBase, String tableName, String editSecret) {
        Preconditions.checkArgument(dataBase != null, "dataSource must not be null");
        Preconditions.checkArgument(tableName != null, "tableName must not be null");
        Preconditions.checkArgument(editSecret != null, "editSecret must not be null");

        this.dataBase = dataBase;
        this.tableName = tableName;
        this.editSecret = editSecret;
    }

    @Override
    public List<Map<String, Object>> getServers() throws IOException {
        try {
            List<Map<String, Object>> servers = dataBase.readAll(tableName);
            return servers;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public Result addServer(String name, String address, int port, String owner, String secret) {

        try {
            Result response = verify(name, address, port, owner, secret);
            if (!response.isSuccess()) {
                return response;
            } else {
                dataBase.insert(tableName, name, address, port, owner);
                return Result.success("Entry added!");
            }
        } catch (Exception e) {
            logger.error("Could not insert entry into server table: " + e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    @Override
    public Result removeServer(String address, int port, String secret) {

        if (address == null) {
            return Result.fail("No address specified");
        }

        if (port == 0) {
            return Result.fail("No port specified");
        }

        if (!editSecret.equals(secret)) {
            return Result.fail("Invalid secret");
        }

        try {
            if (dataBase.remove(tableName, address, port)) {
                return Result.success("Entry removed!");
            } else {
                return Result.fail("Entry not found");
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @Override
    public Result updateServer(String name, String address, int port, String owner, String secret) {

        Result response = verify(name, address, port, owner, secret);
        if (!response.isSuccess()) {
            return response;
        }

        try {
            if (dataBase.update(tableName, name, address, port, owner)) {
                return Result.success("Entry updated!");
            } else {
                return Result.fail("Entry not found");
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    private Result verify(String name, String address, int port, String owner, String secret) {
        if (name == null) {
            return Result.fail("No name specified");
        }

        if (name.length() < 3 || name.length() > 20) {
            return Result.fail("Name length must be in [3..20]");
        }

        if (port < 1024 || port > 65535) {
            return Result.fail("Port must be in [1024..65535]");
        }

        if (address == null || address.trim().isEmpty()) {
            return Result.fail("No address specified");
        }

        if (owner == null || owner.isEmpty()) {
            return Result.fail("No owner specified");
        }

        try {
            InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            logger.error("Could not resolve host: " + e.getMessage());
            return Result.fail("Unknown host: " + address);
        }

        if (!editSecret.equals(secret)) {
            return Result.fail("Invalid secret");
        }

        return Result.success("OK");
    }
}
