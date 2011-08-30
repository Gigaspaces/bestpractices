package org.openspaces.bestpractices.mirror.cassandra.common;

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

public class TestEDS {
    CassandraTestUtil cassandraUtil = new CassandraTestUtil();
    FileUtil fileUtil = new FileUtil();
    private ProcessingUnitContainer space;
    ProcessingUnitContainer mirror;
    private GigaSpace gigaspace;

    @BeforeTest
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
        System.out.println("----------------------------------\nStarting containers");
        mirror = startMirror();
        space = startSpace();
        gigaspace = new GigaSpaceConfigurer(new UrlSpaceConfigurer("jini://*/*/space")).gigaSpace();
        System.out.println("----------------------------------\nStarted containers");
    }

    @AfterMethod
    public void stopContainers() {
        System.out.println("----------------------------------\nStopping containers");
        space.close();
        mirror.close();
        System.out.println("----------------------------------\nStopped containers");
    }

    @AfterTest
    public void shutdown() {
        cassandraUtil.shutDownCassandra();
    }

    @Test
    public void testEDSMirror() {
       Person person=new Person();
        person.setId("asdlkkjhaskjhd");
        person.setFirstName("John");
        person.setLastName("Public");
        person.setCreditScore(600);
        gigaspace.write(person);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test(dependsOnMethods = {"testEDSMirror"})
    public void testInitialLoad() {

    }
}
