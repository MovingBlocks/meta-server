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

package org.terasology.web.servlet;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.mvc.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.module.ModuleMetadata;
import org.terasology.naming.Name;
import org.terasology.version.Version;
import org.terasology.web.model.ModuleListModel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

/**
 * TODO Type description
 * @author Martin Steiger
 */
@Path("/modules/")
public class ModuleServlet {

    private static final Logger logger = LoggerFactory.getLogger(ModuleServlet.class);

    private final ModuleListModel model;

    public ModuleServlet(ModuleListModel model) {
        this.model = model;
    }


    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Object list() {
        logger.info("Requested module list");

        Set<Name> names = model.findModules();
        // the key needs to be string, so that FreeMarker can use it for lookups
        Multimap<String, org.terasology.naming.Version> map = TreeMultimap.create();
        for (Name name : names) {
            map.putAll(name.toString(), model.findVersions(name));
        }

        return map.asMap();
    }

    @GET
    @Path("show")
    @Produces(MediaType.TEXT_HTML)
    public Viewable show() {
        logger.info("Requested module list as HTML");

        ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                .put("items", list())
                .put("version", Version.getVersion())
                .build();
        return new Viewable("/module-list.ftl", dataModel);
    }

    @GET
    @Path("info/{module}/{version}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable info(@PathParam("module") String module, @PathParam("version") String version) {
        logger.info("Requested module info");

        ModuleMetadata meta = model.findMetadata(new Name(module), new org.terasology.naming.Version(version));

        ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                .put("meta", meta)
                .put("version", Version.getVersion())
                .build();
        return new Viewable("/module-info.ftl", dataModel);
    }
}
