package com.gigaspaces.tutorials.processor;

import com.gigaspaces.tutorials.common.builder.OrderEventBuilder;
import com.gigaspaces.tutorials.common.dao.AccountDataDAO;
import com.gigaspaces.tutorials.common.model.AccountData;
import com.gigaspaces.tutorials.common.model.Operation;
import com.gigaspaces.tutorials.common.model.OrderEvent;
import com.gigaspaces.tutorials.common.model.Status;
import com.gigaspaces.tutorials.common.service.AccountDataService;
import org.openspaces.events.EventDriven;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@EventDriven
@Polling
public class Processor {
  @Autowired
  AccountDataService service;

  private final static BigDecimal NEGATIVE_ONE = new BigDecimal("-1");

  @EventTemplate
  OrderEvent getTemplate() {
    return new OrderEventBuilder().status(Status.PENDING).build();
  }

  @SpaceDataEvent
  OrderEvent handleEvent(OrderEvent event) {
    AccountData data = service.load(event.getUserName(), 1000);
    if (data == null) {
      event.setStatus(Status.ACCOUNT_NOT_FOUND);
      // early exit, bad account managed to get in
      return event;
    }
    BigDecimal change = event.getPrice().multiply(event.getOperation().equals(Operation.BUY)
                                                  ? NEGATIVE_ONE : BigDecimal.ONE);
    if(data.getBalance().add(change).compareTo(BigDecimal.ZERO)==-1) {
      event.setStatus(Status.INSUFFICIENT_FUNDS);
    } else {
      data.setBalance(data.getBalance().add(change));
      event.setStatus(Status.PROCESSED);
      service.save(data);
    }
    return event;
  }
}
