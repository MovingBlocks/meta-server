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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.PooledConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Martin Steiger
 */
public class Serveletty extends HttpServlet {

    private static final long serialVersionUID = 6952921980278075265L;

    private static final Logger logger = LoggerFactory.getLogger(Serveletty.class);

    private final PooledConnection connPool;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Serveletty(PooledConnection pooledConnection) {
        this.connPool = pooledConnection;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        if (req.getRequestURI().endsWith("/list")) {
            showDatabase("servers", resp);
        }
    }

    private void showDatabase(String tableName, HttpServletResponse resp) throws IOException {

        try (Connection connection = connPool.getConnection()) {
            try (Statement stmt = connection.createStatement()) {

                List<Map<String, Object>> entries = Lists.newArrayList();

                try (ResultSet rs = stmt.executeQuery("SELECT * FROM \"" + tableName + "\"")) {

                    ResultSetMetaData metaData = rs.getMetaData();
                    while (rs.next()) {
                        Map<String, Object> entry = Maps.newHashMap();

                        for (int i = 1; i <= metaData.getColumnCount(); i++) {
                            entry.put(metaData.getColumnLabel(i), rs.getObject(i));
                        }
                        entries.add(entry);
                    }
                }

                String result = gson.toJson(entries);
                resp.getWriter().print(result);
            }
        }
        catch (SQLException e) {
            logger.warn("There was an SQL error: ", e);
            throw new IOException(e);
        }
    }
}
