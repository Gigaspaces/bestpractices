package com.eauction.model;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
public class UserIntegrationTest extends BaseTest {
    @Before
    @After
    public void clearSpace() {
        gigaSpace.clean();
    }

    @Test
    public void testUser() {
        User user = new User();
        gigaSpace.write(user);
        user = gigaSpace.read(user);
        assertNotNull(user.getId());
    }

    @Test
    public void testUserPartitioning1() {
        User user = new User();
        user.setRoutingId(0);
        gigaSpacePartitionClustered.write(user);

        user = gigaSpacePartition1.read(new User());
        assertNotNull(user);
    }

    @Test
    public void testUserPartitioning2() {
        User user = new User();
        user.setRoutingId(1);
        gigaSpacePartitionClustered.write(user);

        user = gigaSpacePartition2.read(new User());
        assertNotNull(user);
    }

    @Test
    public void testUserPartitioning3() {
        assertEquals(gigaSpacePartitionClustered.count(new User()), 2);
    }
}
