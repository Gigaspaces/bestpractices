package org.openspaces.bestpractices.mirror.cassandra.common;

import org.openspaces.bestpractices.mirror.common.tests.AbstractTestEDS;
import org.openspaces.bestpractices.mirror.common.tests.FileCallback;
import org.openspaces.bestpractices.mirror.common.tests.FileUtil;
import org.openspaces.bestpractices.mirror.model.Person;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.*;

import java.io.IOException;

import static org.testng.Assert.assertNotNull;

public class TestCassandraEDS extends AbstractTestEDS {
    CassandraTestUtil cassandraUtil = new CassandraTestUtil();
    private FileUtil fileUtil=new FileUtil();

    @BeforeClass
    public void startCassandra() throws IOException {
final String ref[] = new String[1];
        FileCallback fileCallback = new FileCallback() {
            @Override
            public void found(String filename) {
                ref[0] = filename;
            }
        };
        fileUtil.findFile("cassandra\\.yaml", fileCallback);
        System.out.println(ref[0]);
        ref[0] = ref[0].substring(!ref[0].contains(":") ? 0 : ref[0].indexOf(":") + 1);
        cassandraUtil.startCassandra("file:" + ref[0]);
    }

    @AfterClass
    public void shutdownCassandra() {
        cassandraUtil.shutDownCassandra();
    }
}
