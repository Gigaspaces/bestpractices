package org.openspaces.bestpractices.mirror.mongodb.common;

import com.gigaspaces.document.DocumentProperties;
import com.gigaspaces.document.SpaceDocument;
import org.openspaces.bestpractices.mirror.model.Person;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

public class TestDocumentPropertiesNonMirrored {
    private ProcessingUnitContainer space;
    private GigaSpace gigaspace;

    ProcessingUnitContainer startSpace() {
        IntegratedProcessingUnitContainerProvider provider = new IntegratedProcessingUnitContainerProvider();
        provider.addConfigLocation(new ClassPathResource("/nonmirrored-space.xml"));
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
        space = startSpace();
        gigaspace = new GigaSpaceConfigurer(new UrlSpaceConfigurer("jini://*/*/space")).gigaSpace();
        System.out.println("----------------------------------\nStarted containers");
    }

    @AfterMethod
    public void stopContainers() {
        System.out.println("----------------------------------\nStopping containers");
        space.close();
        System.out.println("----------------------------------\nStopped containers");
    }

    @Test
    public void testEDSMirror() {
        Person p = new Person();
        Person[] people = gigaspace.takeMultiple(p, Integer.MAX_VALUE);

        Person person = new Person();
        person.setFirstName("John");
        person.setLastName("Public");
        person.setCreditScore(600);
        gigaspace.write(person);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test(dependsOnMethods = {"testEDSMirror"})
    public void testInitialLoad() {
        Person p = new Person();
        Person person = gigaspace.take(p);
        System.out.println(person + " (should not be null)");
        //assertNotNull(person);
    }

    @Test(dependsOnMethods = "testEDSMirror")
    public void testSpaceDocumentMirror() {
        DocumentProperties properties = new DocumentProperties()
                .setProperty("CatalogNumber", "12345")
                .setProperty("Category", "a")
                .setProperty("Price", 1.20)
                .setProperty("Name", "product name 12345");

        SpaceDocument document = new SpaceDocument("Product", properties);
        // 3. Write the document to the space:
        gigaspace.write(document);
        System.out.println(document+" written");
    }
}
