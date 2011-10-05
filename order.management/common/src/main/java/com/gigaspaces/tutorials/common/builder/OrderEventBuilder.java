package com.gigaspaces.tutorials.common.builder;

import com.gigaspaces.tutorials.common.model.Operation;
import com.gigaspaces.tutorials.common.model.OrderEvent;
import com.gigaspaces.tutorials.common.model.Status;

import java.math.BigDecimal;

public class OrderEventBuilder extends AbstractBuilder<OrderEvent> {
  public OrderEventBuilder status(Status status) {
    instance.setStatus(status);
    return this;
  }

  public OrderEventBuilder operation(Operation operation) {
    instance.setOperation(operation);
    return this;
  }

  public OrderEventBuilder price(int price) {
    instance.setPrice(new BigDecimal(price));
    return this;
  }

  public OrderEventBuilder price(double price) {
    instance.setPrice(new BigDecimal(price));
    return this;
  }

  public OrderEventBuilder price(String price) {
    instance.setPrice(new BigDecimal(price));
    return this;
  }

  public OrderEventBuilder price(BigDecimal price) {
    instance.setPrice(price);
    return this;
  }

  public OrderEventBuilder username(String username) {
    instance.setUserName(username);
    return this;
  }

  public OrderEventBuilder id(String id) {
    instance.setId(id);
    return this;
  }
}
