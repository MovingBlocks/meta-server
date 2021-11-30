package org.terasology.module;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.web.model.artifactory.ArtifactoryRepo;
import org.terasology.web.services.impl.ModuleListServiceImpl;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Paths;

@Singleton
public class RepositoryRegistrar implements ApplicationEventListener<StartupEvent> {
    private final static Logger logger = LoggerFactory.getLogger(RepositoryRegistrar.class);

    @Inject
    Provider<ModuleListServiceImpl> moduleListServiceProvider;

    @Override
    public void onApplicationEvent(StartupEvent event) {
        logger.info("Register Repositories");
        try {
            ArtifactoryRepo repo = ArtifactoryRepo.release("http://artifactory.terasology.org/artifactory", "virtual-repo-live", "org/terasology/modules", Paths.get("cache"));
            moduleListServiceProvider.get().addRepository(
                    repo
            );
        } catch (IOException e) {
            logger.error("Cannot register modules repository" + e);
        }
    }
}
