package org.terasology.web.services.impl;

import io.micronaut.context.annotation.EachProperty;

@EachProperty(value = "meta-server.module.repos", list = true)
public class ArtifactRepositoryConfig {
    private String url;
    private String repoName;
    private String group;
    private String cacheFolder;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getCacheFolder() {
        return cacheFolder;
    }

    public void setCacheFolder(String cacheFolder) {
        this.cacheFolder = cacheFolder;
    }
}
