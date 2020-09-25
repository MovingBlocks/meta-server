package org.terasology.master;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.terasology.master.services.DummyArtifactRepo;
import org.terasology.web.artifactory.ArtifactRepository;
import org.terasology.web.db.DataBase;
import org.terasology.web.geo.GeoLocation;
import org.terasology.web.geo.GeoLocationService;
import org.terasology.web.model.ModuleListModelImpl;
import org.terasology.web.model.ServerEntry;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.SQLException;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseTests {

    private static final String SERVER_TABLE = "servers";
    protected ServerEntry firstEntry;
    @Inject
    ModuleListModelImpl moduleListModel;
    @Inject
    DataBase dataBase;
    @Inject
    GeoLocationService geoService;

    protected DummyArtifactRepo snapshotRepo;

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
        dataBase.createTable(SERVER_TABLE);
        GeoLocation geo = geoService.resolve("localhost");
        firstEntry = new ServerEntry("localhost", 25000);
        firstEntry.setName("myName");
        firstEntry.setOwner("Tester");
        firstEntry.setCountry(geo.getCountry());
        firstEntry.setStateprov(geo.getStateOrProvince());
        firstEntry.setCity(geo.getCity());
        dataBase.insert(SERVER_TABLE, firstEntry);
    }
}
