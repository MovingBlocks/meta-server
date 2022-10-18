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

package org.terasology.web.services.impl.geo.dbip;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.terasology.web.services.api.GeoLocationService;
import org.terasology.web.services.impl.geo.GeoLocation;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Resolves geo-location for a hostname or IP address based on db-ip.com.
 * Requires a system environment variable "DBIP_API_KEY" with a valid API key.
 */
@Singleton
public class DbIpGeoLocationService implements GeoLocationService {

    private final String apiKey;

    private final DbIpRestWrapper dbIpRestWrapper;

    public DbIpGeoLocationService(
            DbIpRestWrapper dbIpRestWrapper,
            @Value("${meta-server.dbip.api.key}") String apiKey
    ) {
        this.apiKey = apiKey;
        this.dbIpRestWrapper = dbIpRestWrapper;
    }

    @Override
    public GeoLocation resolve(String hostnameOrIp) throws IOException {

        InetAddress inet = InetAddress.getByName(hostnameOrIp);
        String ipAddress = inet.getHostAddress();

        DbIpQueryResponse response = dbIpRestWrapper.getGeoLocation(ipAddress, apiKey);
        if (response.isSuccess()) {
            return response;
        } else {
            throw new IOException(response.getError());
        }
    }
}
