package org.openspaces.rollingupgrade.processor;

import org.openspaces.rollingupgrade.common.Data;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.openspaces.core.GigaSpace;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;


/**
 * Integration test for the Processor. Uses similar xml definition file (ProcessorIntegrationTest-context.xml)
 * to the actual pu.xml. Writs an unprocessed Data to the Space, and verifies that it has been processed by
 * taking a processed one from the space.
 */
@ContextConfiguration
public class ProcessorIntegrationTest extends AbstractTestNGSpringContextTests{

    @Autowired
    GigaSpace gigaSpace;

    @BeforeMethod
    @AfterMethod
    public void clearSpace() {
        gigaSpace.clear(null);
    }

    @Test
    public void verifyProcessing() throws Exception {
        // write the data to be processed to the Space
        Data data = new Data(1, "test");
        gigaSpace.write(data);

        // create a template of the processed data (processed)
        Data template = new Data();
        template.setType(1l);
        template.setProcessed(true);

        // wait for the result
        Data result = gigaSpace.take(template, 500);
        // verify it
        assertNotNull(result);
        assertTrue(result.isProcessed());
        assertEquals("PROCESSED : " + data.getRawData(), result.getData());
    }

}
