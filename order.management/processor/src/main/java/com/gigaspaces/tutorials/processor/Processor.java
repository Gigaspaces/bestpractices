package com.gigaspaces.tutorials.processor;

import com.gigaspaces.tutorials.common.builder.OrderEventBuilder;
import com.gigaspaces.tutorials.common.model.AccountData;
import com.gigaspaces.tutorials.common.model.Operation;
import com.gigaspaces.tutorials.common.model.OrderEvent;
import com.gigaspaces.tutorials.common.model.Status;
import com.gigaspaces.tutorials.common.service.AccountDataService;
import org.openspaces.events.EventDriven;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@EventDriven
@Polling
public class Processor {
  @Autowired
  AccountDataService service;

  Logger logger = LoggerFactory.getLogger(this.getClass());

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
      if (logger.isInfoEnabled()) {
        logger.info("Order processed: " + event);
      }
      return event;
    }
    BigDecimal change = event.getPrice().multiply(event.getOperation().equals(Operation.BUY)
                                                  ? NEGATIVE_ONE : BigDecimal.ONE);
    if (data.getBalance().add(change).compareTo(BigDecimal.ZERO) == -1) {
      event.setStatus(Status.INSUFFICIENT_FUNDS);
    } else {
      data.setBalance(data.getBalance().add(change));
      event.setStatus(Status.PROCESSED);
      if (logger.isInfoEnabled()) {
        logger.info("Account changed: " + data);
      }
      service.save(data);
    }
    if (logger.isInfoEnabled()) {
      logger.info("Order processed: " + event);
    }
    return event;
  }
}
