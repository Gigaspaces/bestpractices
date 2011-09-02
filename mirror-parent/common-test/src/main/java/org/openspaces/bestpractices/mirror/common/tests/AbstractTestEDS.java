package org.openspaces.bestpractices.mirror.common.tests;

import com.gigaspaces.document.DocumentProperties;
import com.gigaspaces.document.SpaceDocument;
import com.j_spaces.core.client.UpdateModifiers;
import net.jini.core.lease.Lease;
import org.openspaces.bestpractices.mirror.model.Person;
import org.openspaces.bestpractices.mirror.model.PersonBuilder;
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

import static org.testng.Assert.*;

public class AbstractTestEDS {
    protected ProcessingUnitContainer space;
    protected ProcessingUnitContainer mirror;
    protected GigaSpace gigaspace;

    public ProcessingUnitContainer startMirror() {
        return startMirror("/mirror.xml");
    }

    public ProcessingUnitContainer startSpace() {
        return startSpace("/space.xml");
    }

    public ProcessingUnitContainer startMirror(String resource) {
        IntegratedProcessingUnitContainerProvider provider = new IntegratedProcessingUnitContainerProvider();
        provider.addConfigLocation(new ClassPathResource(resource));
        return provider.createContainer();
    }

    public ProcessingUnitContainer startSpace(String resource) {
        IntegratedProcessingUnitContainerProvider provider = new IntegratedProcessingUnitContainerProvider();
        provider.addConfigLocation(new ClassPathResource(resource));
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
    public void stopContainers() throws InterruptedException {
        Thread.sleep(1000);
        System.out.println("----------------------------------\nStopping containers");
        space.close();
        mirror.close();
        System.out.println("----------------------------------\nStopped containers");
    }

    @Test
    public void clearSpace() {
        gigaspace.clear(new Object());
    }

    @Test(dependsOnMethods = "clearSpace")
    public void storePOJOToTestMirror() {
        gigaspace.write(new PersonBuilder().creditScore(620)
                .firstName("Samuel").lastName("Hamilton").build());
        gigaspace.write(new PersonBuilder().creditScore(600)
                .firstName("Alexander").lastName("Franklin").build());

    }

    @Test(dependsOnMethods = "storePOJOToTestMirror")
    public void testPOJOFromInitialLoadAndUpdate() {
        Person template = new PersonBuilder().lastName("Hamilton").build();
        Person person = gigaspace.readIfExists(template);
        assertNotNull(person);
        assertEquals(620, person.getCreditScore().intValue());
        assertEquals(person.getFirstName(), "Samuel");
        person.setCreditScore(630); // woo, credit score went up!
        gigaspace.write(person, Lease.FOREVER, 100, UpdateModifiers.PARTIAL_UPDATE);
    }

    @Test(dependsOnMethods = "testPOJOFromInitialLoadAndUpdate")
    public void testPOJOUpdatedAndRemove() {
        Person template = new PersonBuilder().lastName("Hamilton").build();
        Person person = gigaspace.takeIfExists(template);
        assertNotNull(person);
        assertEquals(630, person.getCreditScore().intValue()); // note updated credit score
        assertEquals(person.getFirstName(), "Samuel");
    }

    @Test(dependsOnMethods = "testPOJOUpdatedAndRemove")
    public void testPOJORemovedData() {
        Person template = new PersonBuilder().lastName("Hamilton").build();
        Person person = gigaspace.takeIfExists(template);
        assertNull(person);
    }

    @Test(dependsOnMethods = "clearSpace")
    public void storeDocumentToTestMirror() {
        gigaspace.write(new SpaceDocument("Product",
                new DocumentProperties()
                        .setProperty("CatalogNumber", "00001")
                        .setProperty("Name", "Product 1")
                        .setProperty("Category", "hardware")
                        .setProperty("Price", 15.00)
        ));
        gigaspace.write(new SpaceDocument("Product",
                new DocumentProperties()
                        .setProperty("CatalogNumber", "00002")
                        .setProperty("Name", "Product 2")
                        .setProperty("Category", "software")
                        .setProperty("Price", 25.00)
        ));
    }

    @Test(dependsOnMethods = "storeDocumentToTestMirror")
    public void testDocumentFromInitialLoadAndUpdate() {
        SpaceDocument template = new SpaceDocument("Product",
                new DocumentProperties()
                        .setProperty("CatalogNumber", "00001"));
        SpaceDocument product = gigaspace.readIfExists(template);
        assertNotNull(product);
        assertEquals(15.00, product.getProperty("Price"));
        assertEquals("Product 1", product.getProperty("Name"));
        assertEquals("hardware", product.getProperty("Category"));
        product.setProperty("Price", 13.99); // woo, price went DOWN
        // can't use PARTIAL_UPDATE for dynamic properties
        gigaspace.write(product, Lease.FOREVER, 100, UpdateModifiers.UPDATE_OR_WRITE);
    }

    @Test(dependsOnMethods = "testDocumentFromInitialLoadAndUpdate")
    public void testDocumentUpdatedAndRemove() {
        SpaceDocument template = new SpaceDocument("Product",
                new DocumentProperties()
                        .setProperty("CatalogNumber", "00001"));
        SpaceDocument product = gigaspace.takeIfExists(template);
        assertNotNull(product);
        assertEquals(13.99, product.getProperty("Price"));
        assertEquals("Product 1", product.getProperty("Name"));
        assertEquals("hardware", product.getProperty("Category"));
    }

    @Test(dependsOnMethods = "testDocumentUpdatedAndRemove")
    public void testDocumentRemovedData() {
        SpaceDocument template = new SpaceDocument("Product",
                new DocumentProperties()
                        .setProperty("CatalogNumber", "00001"));
        SpaceDocument product = gigaspace.readIfExists(template);
        assertNull(product);
    }
}
