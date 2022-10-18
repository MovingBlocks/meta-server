package org.terasology.web.services.impl;

import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.web.model.artifactory.ArtifactRepository;
import org.terasology.web.model.artifactory.ArtifactoryRepo;

import java.io.IOException;
import java.nio.file.Paths;

@Factory
public class RepositoryFactory {
    private final static Logger logger = LoggerFactory.getLogger(RepositoryFactory.class);


    @EachBean(ArtifactRepositoryConfig.class)
    public ArtifactRepository repository(ArtifactRepositoryConfig repoConfig) {
        try {
            return ArtifactoryRepo.release(
                    repoConfig.getUrl(),
                    repoConfig.getRepoName(),
                    repoConfig.getGroup(),
                    Paths.get(repoConfig.getCacheFolder()));
        } catch (IOException e) {
            logger.error("Cannot register Repository", e);
        }
        return null;
    }
}
