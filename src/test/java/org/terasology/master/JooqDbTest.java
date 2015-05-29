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

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.terasology.web.JooqDatabase;

public class JooqDbTest {

    // Keep the content of an in-memory database as long as the virtual machine is alive
    private static final String DB_URL = "jdbc:h2:mem:dbtest;DB_CLOSE_DELAY=-1";

    @Test
    public void testConnection() throws Exception {

        JooqDatabase db = new JooqDatabase(DB_URL);
        String tableName = "servers";

        db.createTable(tableName);
        db.insert(tableName, "myName", "localhost", 25000, "Tester");

        Map<String, Object> data = db.readAll(tableName).get(0);

        Assert.assertTrue(data.get("name").equals("myName"));
        Assert.assertTrue(data.get("owner").equals("Tester"));
        Assert.assertTrue(data.get("port").equals(25000));
        Assert.assertTrue(data.get("address").equals("localhost"));
    }
}
