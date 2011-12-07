package com.gigaspaces.cmdline;

import com.gigaspaces.tutorials.common.model.OrderEvent;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.SQLQuery;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

public class CommandLineClient {
    public static void main(String... args) {
        UrlSpaceConfigurer configurer = new UrlSpaceConfigurer("jini://*/*/orderManagement");
        IJSpace space = configurer.space();
        GigaSpace gigaSpace = new GigaSpaceConfigurer(space).gigaSpace();
        SQLQuery<OrderEvent> query = new SQLQuery<OrderEvent>(OrderEvent.class,
                "order by lastUpdateTime DESC");
        while (true) {
            OrderEvent event = gigaSpace.read(query, 1000);
            if (event != null) {
                System.out.println(event);
            }
        }
    }
}
