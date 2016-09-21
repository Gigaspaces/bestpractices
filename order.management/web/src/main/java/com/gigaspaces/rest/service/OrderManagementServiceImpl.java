package com.gigaspaces.rest.service;

import com.gigaspaces.tutorials.common.model.AccountData;
import com.gigaspaces.tutorials.common.model.OrderEvent;
import com.gigaspaces.tutorials.common.service.AccountDataService;
import com.gigaspaces.tutorials.common.service.OrderEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderManagementServiceImpl implements OrderManagementService {
    @Autowired
    OrderEventService orderEventService;

    @Autowired
    AccountDataService accountDataService;

    @Override
    public OrderEvent[] getOrderEvents() {
        return orderEventService.getAllOrderEvents();
    }

    @Override
    public AccountData[] getAccountData() {
        return accountDataService.getAllAccountData();
    }
}
