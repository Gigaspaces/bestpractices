package com.gigaspaces.tutorials.common.service;

import com.gigaspaces.tutorials.common.model.OrderEvent;

public interface OrderEventService {
    void post(OrderEvent orderEvent);

    OrderEvent[] getAllOrderEvents();
}
