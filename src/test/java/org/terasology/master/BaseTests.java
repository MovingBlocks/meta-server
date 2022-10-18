package org.terasology.master;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.terasology.master.services.DummyArtifactRepo;
import org.terasology.web.model.artifactory.ArtifactRepository;
import org.terasology.web.model.server.ServerEntry;
import org.terasology.web.services.api.DatabaseService;
import org.terasology.web.services.api.GeoLocationService;
import org.terasology.web.services.impl.ModuleListServiceImpl;
import org.terasology.web.services.impl.geo.GeoLocation;

import java.io.IOException;
import java.sql.SQLException;

@MicronautTest(environments = "test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseTests {

    private static final String SERVER_TABLE = "servers";
    protected ServerEntry firstEntry;
    protected DummyArtifactRepo snapshotRepo;
    @Inject
    ModuleListServiceImpl moduleListModel;
    @Inject
    DatabaseService databaseService;
    @Inject
    GeoLocationService geoService;

    @BeforeAll
    void setupModules() throws IOException, SQLException {
        DummyArtifactRepo releaseRepo = new DummyArtifactRepo(ArtifactRepository.RepoType.RELEASE);
        try {
            releaseRepo.addArtifact("Core", new ClasspathArtifactInfo("/metas/" + "Core-0.53.1.jar_info.json"));

            snapshotRepo = new DummyArtifactRepo(ArtifactRepository.RepoType.SNAPSHOT);
            snapshotRepo.addArtifact("ChrisVolume1OST", new ClasspathArtifactInfo("/metas/" + "ChrisVolume1OST-0.2.1-20150608.034649-1.jar_info.json"));
            snapshotRepo.addArtifact("MusicDirector", new ClasspathArtifactInfo("/metas/" + "MusicDirector-0.2.1-20150608.041945-1.jar_info.json"));

            moduleListModel.addRepository(releaseRepo);
            moduleListModel.addRepository(snapshotRepo);
            moduleListModel.updateAllModules();
        } catch (IOException e) {

        }
    }

    @BeforeAll
    void setupDatabase() throws IOException, SQLException {
        databaseService.createTable(SERVER_TABLE);
        GeoLocation geo = geoService.resolve("localhost");
        firstEntry = new ServerEntry("localhost", 25000);
        firstEntry.setName("myName");
        firstEntry.setOwner("Tester");
        firstEntry.setCountry(geo.getCountry());
        firstEntry.setStateprov(geo.getStateOrProvince());
        firstEntry.setCity(geo.getCity());
        databaseService.insert(SERVER_TABLE, firstEntry);
    }
}
