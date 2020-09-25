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
import org.terasology.web.services.api.GeoLocationService;
import org.terasology.web.services.impl.geo.GeoLocation;
import retrofit.RestAdapter;

import javax.inject.Singleton;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Resolves geo-location for a hostname or IP address based on db-ip.com.
 * Requires a system environment variable "DBIP_API_KEY" with a valid API key.
 */
@Singleton
public class GeoLocationServiceDbIp implements GeoLocationService {

    private final String apiKey;

    public GeoLocationServiceDbIp(
            @Value("meta-server.dbip.api.key") String apiKey
    ) {
        this.apiKey = apiKey;
    }

    @Override
    public GeoLocation resolve(String hostnameOrIp) throws IOException {
        String url = "http://api.db-ip.com/";

        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(url).build();
        DbIpRestWrapper service = restAdapter.create(DbIpRestWrapper.class);

        InetAddress inet = InetAddress.getByName(hostnameOrIp);
        String ipAddress = inet.getHostAddress();

        DbIpQueryResponse response = service.getGeoLocation(ipAddress, apiKey);
        if (response.isSuccess()) {
            return response;
        } else {
            throw new IOException(response.getError());
        }
    }
}
