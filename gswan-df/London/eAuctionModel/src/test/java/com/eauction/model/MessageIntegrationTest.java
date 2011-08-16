package com.eauction.model;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
public class MessageIntegrationTest extends BaseTest {
    @Before
    @After
    public void clearSpace() {
        gigaSpace.clean();
    }

    @Test
    public void testMessage() {
        Message message = new Message();
        gigaSpace.write(message);
        message = gigaSpace.read(message);
        assertNotNull(message.getId());
    }

    @Test
    public void testMessagePartitioning1() {
        Message message = new Message();
        message.setRoutingId(0);
        gigaSpacePartitionClustered.write(message);

        message = gigaSpacePartition1.read(new Message());
        assertNotNull(message);
    }

    @Test
    public void testMessagePartitioning2() {
        Message message = new Message();
        message.setRoutingId(1);
        gigaSpacePartitionClustered.write(message);

        message = gigaSpacePartition2.read(new Message());
        assertNotNull(message);
    }

    @Test
    public void testMessagePartitioning3() {
        assertEquals(gigaSpacePartitionClustered.count(new Message()), 2);
    }
}
