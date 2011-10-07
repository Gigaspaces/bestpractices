package com.gigaspaces.tutorials.common.service.impl;

import com.gigaspaces.tutorials.common.dao.OrderEventDAO;
import com.gigaspaces.tutorials.common.model.OrderEvent;
import com.gigaspaces.tutorials.common.service.OrderEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderEventServiceImpl implements OrderEventService {
  @Autowired
  OrderEventDAO dao;

  @Override
  public void post(OrderEvent orderEvent) {
    dao.write(orderEvent);
  }

  @Override
  public OrderEvent[] getAllOrderEvents() {
    return dao.readMultiple(new OrderEvent());
  }
}
