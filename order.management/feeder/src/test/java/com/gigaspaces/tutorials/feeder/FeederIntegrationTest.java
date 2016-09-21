package com.gigaspaces.tutorials.feeder;

import com.gigaspaces.tutorials.common.model.AccountData;
import com.gigaspaces.tutorials.common.model.OrderEvent;
import com.gigaspaces.tutorials.common.model.Status;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration
public class FeederIntegrationTest extends AbstractTestNGSpringContextTests {
    @Autowired
    GigaSpace gigaSpace;

    @Test
    public void checkPreload() {
        AccountData[] data = gigaSpace.readMultiple(new AccountData(), Integer.MAX_VALUE);
        System.out.println(data.length);
        assertEquals(data.length, 1000);
    }

    @Test
    public void checkFeeder() throws InterruptedException {
        Thread.sleep(4000);
        OrderEvent[] events = gigaSpace.readMultiple(new OrderEvent(), Integer.MAX_VALUE);
        assertTrue(events.length > 3);
        for (OrderEvent event : events) {
            assertEquals(event.getStatus(), Status.NEW);
        }
    }
}
