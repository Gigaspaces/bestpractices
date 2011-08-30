package org.openspaces.bestpractices.mirror.cassandra.common;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.*;

import java.io.IOException;

public class TestEDS {
    CassandraTestUtil cassandraUtil = new CassandraTestUtil();
    FileUtil fileUtil = new FileUtil();
    private ProcessingUnitContainer space;
    ProcessingUnitContainer mirror;
    private GigaSpace gigaspace;

    @BeforeClass
    public void startup() throws IOException {
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

    public ProcessingUnitContainer startMirror() {
        IntegratedProcessingUnitContainerProvider provider = new IntegratedProcessingUnitContainerProvider();
        provider.addConfigLocation(new ClassPathResource("/mirror.xml"));
        return provider.createContainer();
    }

    ProcessingUnitContainer startSpace() {
        IntegratedProcessingUnitContainerProvider provider = new IntegratedProcessingUnitContainerProvider();
        provider.addConfigLocation(new ClassPathResource("/space.xml"));
        ClusterInfo ci = new ClusterInfo();
        ci.setNumberOfInstances(1);
        ci.setNumberOfBackups(1);
        ci.setSchema("partitioned-sync2backup");
        provider.setClusterInfo(ci);
        return provider.createContainer();
    }

    @BeforeMethod
    public void startContainers() {
        mirror = startMirror();
        space = startSpace();
        gigaspace = new GigaSpaceConfigurer(new UrlSpaceConfigurer("jini://*/*/space")).gigaSpace();
    }

    @AfterMethod
    public void stopContainers() {
        space.close();
        mirror.close();
    }

    @AfterClass
    public void shutdown() {
        cassandraUtil.shutDownCassandra();
    }

    @Test
    public void testEDSMirror() {

    }

    @Test(dependsOnMethods = {"testEDSMirror"})
    public void testInitialLoad() {

    }
}
