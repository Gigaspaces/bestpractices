package com.gigaspaces.tutorials.validator;

import com.gigaspaces.tutorials.common.builder.AccountDataBuilder;
import com.gigaspaces.tutorials.common.builder.OrderEventBuilder;
import com.gigaspaces.tutorials.common.dao.OrderEventDAO;
import com.gigaspaces.tutorials.common.model.AccountData;
import com.gigaspaces.tutorials.common.model.Operation;
import com.gigaspaces.tutorials.common.model.OrderEvent;
import com.gigaspaces.tutorials.common.model.Status;
import com.gigaspaces.tutorials.common.service.AccountDataService;
import com.gigaspaces.tutorials.common.service.OrderEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@ContextConfiguration
public class ValidatorIntegrationTest extends AbstractTestNGSpringContextTests {
  @Autowired
  AccountDataService accountService;
  @Autowired
  OrderEventService orderService;
  @Autowired
  OrderEventDAO dao;

  @BeforeMethod
  public void setupData() {
    AccountData data = new AccountDataBuilder().username("1234").balance(100).build();
    System.out.println(accountService);
    accountService.save(data);
  }

  @Test
  public void testEventHandling() {
    OrderEvent event = new OrderEventBuilder()
                       .id("1234")
                       .username("1234")
                       .status(Status.NEW)
                       .operation(Operation.BUY)
                       .price(12)
                       .build();
    orderService.post(event);
    OrderEvent template = new OrderEventBuilder()
                          .status(Status.PENDING).build();
    OrderEvent processedEvent = dao.read(template, 1000);
    assertNotNull(processedEvent);
    assertEquals(Status.PENDING, processedEvent.getStatus());

    event = new OrderEventBuilder()
            .id("1234")
            .username("1235")
            .status(Status.NEW)
            .operation(Operation.BUY)
            .price(12)
            .build();
    orderService.post(event);
    template = new OrderEventBuilder()
               .status(Status.ACCOUNT_NOT_FOUND).build();
    processedEvent = dao.read(template, 1000);
    assertNotNull(processedEvent);
    assertEquals(Status.ACCOUNT_NOT_FOUND, processedEvent.getStatus());
  }
}
