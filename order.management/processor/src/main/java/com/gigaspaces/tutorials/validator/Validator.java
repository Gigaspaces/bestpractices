package com.gigaspaces.tutorials.validator;

import com.gigaspaces.tutorials.common.builder.OrderEventBuilder;
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

@EventDriven
@Polling
public class Validator {
    @Autowired
    AccountDataService service;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @EventTemplate
    public OrderEvent getTemplate() {
        return new OrderEventBuilder()
               .status(Status.NEW)
               .build();
    }

    @SpaceDataEvent
    public OrderEvent handleEvent(OrderEvent event) {
        if (!service.accountExists(event.getUsername())) {
            event.setStatus(Status.ACCOUNT_NOT_FOUND);
        } else {
            event.setStatus(Status.PENDING);
        }
        // build a new event - necessary?
        event.setId(null);
        if (logger.isInfoEnabled()) {
            logger.info("Validation result: " + event);
        }
        return event;
    }
}
