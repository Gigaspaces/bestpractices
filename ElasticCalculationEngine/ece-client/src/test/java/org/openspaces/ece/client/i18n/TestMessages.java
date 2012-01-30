package org.openspaces.ece.client.i18n;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class TestMessages {
    @Test
    public void testMessagesLoad() {
        Messages messages=Messages.getInstance();
        assertNotNull(messages);
        assertEquals(messages.getMessage("nonexistent.property.here", "novalue"), "novalue");
    }
}
