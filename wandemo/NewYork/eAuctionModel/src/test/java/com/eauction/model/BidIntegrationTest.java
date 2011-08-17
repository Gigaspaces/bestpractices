package com.eauction.model;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
public class BidIntegrationTest extends BaseTest {
    @Before
    @After
    public void clearSpace() {
        gigaSpace.clean();
    }

    @Test
    public void testBid() {
        Bid bid = new Bid();
        gigaSpace.write(bid);
        bid = gigaSpace.read(bid);
        assertNotNull(bid.getId());
    }

    @Test
    public void testBidPartitioning1() {
        Bid bid = new Bid();
        bid.setRoutingId(0);
        gigaSpacePartitionClustered.write(bid);

        bid = gigaSpacePartition1.read(new Bid());
        assertNotNull(bid);
    }

    @Test
    public void testBidPartitioning2() {
        Bid bid = new Bid();
        bid.setRoutingId(1);
        gigaSpacePartitionClustered.write(bid);

        bid = gigaSpacePartition2.read(new Bid());
        assertNotNull(bid);
    }

    @Test
    public void testBidPartitioning3() {
        assertEquals(gigaSpacePartitionClustered.count(new Bid()), 2);
    }
}
