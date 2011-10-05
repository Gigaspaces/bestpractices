package com.gigaspaces.tutorials.processor;

import com.gigaspaces.tutorials.common.builder.AccountDataBuilder;
import com.gigaspaces.tutorials.common.builder.OrderEventBuilder;
import com.gigaspaces.tutorials.common.model.AccountData;
import com.gigaspaces.tutorials.common.model.Operation;
import com.gigaspaces.tutorials.common.model.OrderEvent;
import com.gigaspaces.tutorials.common.model.Status;
import com.gigaspaces.tutorials.common.service.AccountDataService;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class ProcessorTest {
  @Test
  public void testProcessor() {
    AccountData data = new AccountDataBuilder()
                       .id("1234")
                       .balance("100")
                       .build();
    AccountDataService service = mock(AccountDataService.class);

    when(service.accountExists("1234")).thenReturn(true);
    when(service.load("1234",1000)).thenReturn(data);

    Processor processor = new Processor();
    processor.service = service;
    OrderEvent event = new OrderEventBuilder()
                       .id("1234")
                       .username("1234")
                       .status(Status.NEW)
                       .operation(Operation.BUY)
                       .price("12")
                       .build();
    OrderEvent newEvent = processor.handleEvent(event);
    assertEquals(newEvent.getStatus(), Status.PROCESSED);
    assertEquals(data.getBalance(),new BigDecimal("88"));

    event = new OrderEventBuilder()
                       .id("1234")
                       .username("1234")
                       .status(Status.NEW)
                       .operation(Operation.SELL)
                       .price("12")
                       .build();
    newEvent = processor.handleEvent(event);
    assertEquals(newEvent.getStatus(), Status.PROCESSED);
    assertEquals(data.getBalance(),new BigDecimal("100"));

    event = new OrderEventBuilder()
            .id("1235")
            .username("1235")
            .status(Status.NEW)
            .operation(Operation.BUY)
            .price(12)
            .build();
    newEvent = processor.handleEvent(event);
    assertEquals(newEvent.getStatus(), Status.ACCOUNT_NOT_FOUND);
  }
}
