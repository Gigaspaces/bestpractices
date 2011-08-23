package org.openspaces.rollingupgrade.processor;

import org.openspaces.rollingupgrade.common.Data;

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


/**
 * A simple unit test that verifies the Processor processData method actually processes
 * the Data object.
 */
public class ProcessorTest {

    @Test
    public void verifyProcessedFlag() {
        Processor processor = new Processor();
        Data data = new Data(1, "test");

        Data result = processor.processData(data);
        assertTrue(result.isProcessed());
        assertEquals("PROCESSED : " + data.getRawData(), result.getData());
        assertEquals(data.getType(), result.getType());
    }
}
