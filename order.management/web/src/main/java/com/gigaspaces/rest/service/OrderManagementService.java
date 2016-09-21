package com.gigaspaces.rest.service;

import com.gigaspaces.tutorials.common.model.AccountData;
import com.gigaspaces.tutorials.common.model.OrderEvent;

public interface OrderManagementService {
    OrderEvent[] getOrderEvents();

    AccountData[] getAccountData();
}
