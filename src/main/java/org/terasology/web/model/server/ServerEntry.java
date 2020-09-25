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

package org.terasology.web.model.server;

import java.util.Objects;

/**
 * Server entry database representation.
 */
public class ServerEntry {

    private String address;
    private String name;
    private String owner;
    private String country;
    private String stateprov;
    private String city;
    private int port;
    private boolean active;

    ServerEntry() {
        // required for marshalling
    }

    public ServerEntry(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStateprov() {
        return stateprov;
    }

    public void setStateprov(String stateprov) {
        this.stateprov = stateprov;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "ServerEntry [name=" + name + ", address=" + address + ", port=" + port + ", owner=" + owner + ", active=" + active + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address, port, owner, country, stateprov, city, active);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ServerEntry other = (ServerEntry) obj;

        return Objects.equals(address, other.address)
                && Objects.equals(port, other.port)
                && Objects.equals(name, other.name)
                && Objects.equals(owner, other.owner)
                && Objects.equals(city, other.city)
                && Objects.equals(stateprov, other.stateprov)
                && Objects.equals(country, other.country)
                && Objects.equals(active, other.active);
    }

}
