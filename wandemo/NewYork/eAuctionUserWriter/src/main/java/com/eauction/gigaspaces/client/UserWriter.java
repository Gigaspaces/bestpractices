package com.eauction.gigaspaces.client;

import com.eauction.model.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openspaces.core.GigaSpace;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import java.util.LinkedList;

import javax.annotation.PostConstruct;


@Component
public class UserWriter {
    private final Log log = LogFactory.getLog(UserWriter.class);
    @Autowired
    private GigaSpace gigaSpace;
    @Autowired
    private LinkedList<String> userList;

    @PostConstruct
    public void init() throws Exception {
        log.info("Starting User Feeder");

        int i = 1;

        for (String u : userList) {
            User user = new User();
            user.setRoutingId(i++);

            String[] names = u.split(" ");

            user.setFirstName(names[0]);

            user.setLastName(names[1]);

            User foundUser = gigaSpace.read(user);

            if (foundUser == null) {
                // User is not found, let's add it.
                gigaSpace.write(user);
                log.info(String.format("Added User object with name '%s'",
                        user.getFullName()));
            }
        }

        log.info("Stopping User Feeder");
    }
}
