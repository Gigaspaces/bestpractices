package com.eauction.model;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
public class TradeIntegrationTest extends BaseTest {
    @Before
    @After
    public void clearSpace() {
        gigaSpace.clean();
    }

    @Test
    public void testTrade() {
        Trade trade = new Trade();
        gigaSpace.write(trade);
        trade = gigaSpace.read(trade);
        assertNotNull(trade.getId());
    }

    @Test
    public void testTradePartitioning1() {
        Trade trade = new Trade();
        trade.setRoutingId(0);
        gigaSpacePartitionClustered.write(trade);

        trade = gigaSpacePartition1.read(new Trade());
        assertNotNull(trade);
    }

    @Test
    public void testTradePartitioning2() {
        Trade trade = new Trade();
        trade.setRoutingId(1);
        gigaSpacePartitionClustered.write(trade);

        trade = gigaSpacePartition2.read(new Trade());
        assertNotNull(trade);
    }

    @Test
    public void testTradePartitioning3() {
        assertEquals(gigaSpacePartitionClustered.count(new Trade()), 2);
    }
}
