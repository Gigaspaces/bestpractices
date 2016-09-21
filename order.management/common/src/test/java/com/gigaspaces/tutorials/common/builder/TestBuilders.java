package com.gigaspaces.tutorials.common.builder;

import com.gigaspaces.tutorials.common.model.OrderEvent;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

public class TestBuilders {
    @Test
    public void testOrderBuilder() {
        OrderEvent orderEvent = new OrderEventBuilder()
                            .price(12)
                            .id("129384")
                            .build();
        assertNotNull(orderEvent);
    }
}
