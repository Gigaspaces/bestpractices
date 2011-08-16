package com.eauction.model;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
public class AuctionIntegrationTest extends BaseTest {
    @Before
    @After
    public void clearSpace() {
        gigaSpace.clean();
    }

    @Test
    public void testAuction() {
        Auction auction = new Auction();
        gigaSpace.write(auction);
        auction = gigaSpace.read(auction);
        assertNotNull(auction.getId());
    }

    @Test
    public void testAuctionPartitioning1() {
        Auction auction = new Auction();
        auction.setRoutingId(0);
        gigaSpacePartitionClustered.write(auction);

        auction = gigaSpacePartition1.read(new Auction());
        assertNotNull(auction);
    }

    @Test
    public void testAuctionPartitioning2() {
        Auction auction = new Auction();
        auction.setRoutingId(1);
        gigaSpacePartitionClustered.write(auction);

        auction = gigaSpacePartition2.read(new Auction());
        assertNotNull(auction);
    }

    @Test
    public void testAuctionPartitioning3() {
        assertEquals(gigaSpacePartitionClustered.count(new Auction()), 2);
    }
}
