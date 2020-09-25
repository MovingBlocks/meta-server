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

package org.terasology.web.model.jenkins;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class Build {

    @SerializedName("full_url")
    private String fullUrl;
    private int number;
    private Phase phase;
    private String status;
    private String url;
    private Scm scm;
    private Map<String, String> parameters;
    private StringBuilder log;
    private Map<String, Map<String, String>> artifacts;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> params) {
        this.parameters = new HashMap<>(params);
    }

    public Map<String, Map<String, String>> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(Map<String, Map<String, String>> artifacts) {
        this.artifacts = artifacts;
    }

    public Scm getScm() {
        return scm;
    }

    public void setScm(Scm scmState) {
        this.scm = scmState;
    }

    public StringBuilder getLog() {
        return this.log;
    }

    public void setLog(StringBuilder log) {
        this.log = log;
    }

    public enum Phase {
        STARTED, COMPLETED, FINALIZED
    }
}
