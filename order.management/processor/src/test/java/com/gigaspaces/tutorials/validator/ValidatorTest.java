package com.gigaspaces.tutorials.validator;

import com.gigaspaces.tutorials.common.builder.OrderEventBuilder;
import com.gigaspaces.tutorials.common.model.Operation;
import com.gigaspaces.tutorials.common.model.OrderEvent;
import com.gigaspaces.tutorials.common.model.Status;
import com.gigaspaces.tutorials.common.service.AccountDataService;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class ValidatorTest {
    @Test
    public void testValidator() {
        AccountDataService service = mock(AccountDataService.class);

        when(service.accountExists("1234")).thenReturn(true);

        Validator validator = new Validator();
        validator.service = service;
        OrderEvent event = new OrderEventBuilder()
                           .id("1234")
                           .username("1234")
                           .status(Status.NEW)
                           .operation(Operation.BUY)
                           .price(12)
                           .build();
        OrderEvent newEvent = validator.handleEvent(event);
        assertEquals(newEvent.getStatus(), Status.PENDING);
        assertNull(newEvent.getId());

        event = new OrderEventBuilder()
                .id("1235")
                .username("1235")
                .status(Status.NEW)
                .operation(Operation.BUY)
                .price(12)
                .build();
        newEvent = validator.handleEvent(event);
        assertEquals(newEvent.getStatus(), Status.ACCOUNT_NOT_FOUND);
        assertNull(newEvent.getId());
    }
}
