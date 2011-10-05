package com.gigaspaces.tutorials.common.service;

import com.gigaspaces.tutorials.common.builder.OrderEventBuilder;
import com.gigaspaces.tutorials.common.dao.OrderEventDAO;
import com.gigaspaces.tutorials.common.model.Operation;
import com.gigaspaces.tutorials.common.model.OrderEvent;
import com.gigaspaces.tutorials.common.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@ContextConfiguration
public class OrderEventServicesTest extends AbstractTestNGSpringContextTests {
  @Autowired
  OrderEventService service;
  @Autowired
  OrderEventDAO dao;

  @Test
  public void testOrderEventService() {
    OrderEvent event = new OrderEventBuilder()
                       .id("1234")
                       .status(Status.NEW)
                       .username("hash")
                       .operation(Operation.BUY)
                       .price("100")
                       .build();
    service.post(event);
    OrderEvent e=dao.readById("1234");
    assertEquals(event, e);
  }
}
