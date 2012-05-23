/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openspaces.example.helloworld.feeder;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.example.helloworld.common.*;

import com.j_spaces.core.IJSpace;

import java.util.logging.Logger;

/**
 * This feeder class connects to the space started in the hello-processor module.
 * It then writes 1000 objects of type {@link Message} to space, which are processed by the hello-processor module,
 * It then waits 3000ms and reads from the space one message at random, and finally counts all messages in the space.
 * <p/>
 * This code could be used by a client running anywhere, that wished to connect to a space.
 * Change the argument to the main method or constructor to point at a different space.
 */
public class Feeder {
    Logger logger = Logger.getLogger(this.getClass().getName());
    GigaSpace gigaSpace = null;

    /**
     * This is the main entry point to the Feeder,
     * you should supply the URL of the space to which you wish to connect to,
     * example:  "jini:/&#42;/&#42;/helloSBA" will connect to a space named "helloSBA",
     * If you do not provide a url, the program will exit with a usage message printed.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java Feeder <space URL>");
            System.exit(1);
        }

        Feeder feeder = new Feeder(args[0]);   // create the feeder and connect it to the space

        feeder.feed(1000);                        // run the feeder (start feeding)

        feeder.readResults();                    // read back results
    }

    /**
     * Here we have the only constructor for this example Feeder
     *
     * @param url : the url to the space
     */
    public Feeder(String url) {
        // connect to the space using its URL
        IJSpace space = new UrlSpaceConfigurer(url).space();
        // use gigaspace wrapper to for simpler API
        this.gigaSpace = new GigaSpaceConfigurer(space).gigaSpace();
    }

    /**
     * Feeds the space with messages
     *
     * @param numberOfMessages : number of messages to feed
     */
    public void feed(int numberOfMessages) {
        for (int counter = 0; counter < numberOfMessages; counter++) {
            Message msg = new Message(counter, "Hello ");
            gigaSpace.write(msg);
        }
        logger.info("Feeder WROTE " + numberOfMessages + " messages");
    }

    /**
     * Reads one processed message from the space and prints it,
     * also reads and prints number of total processed messages in the space.
     */
    public void readResults() {
        Message template = new Message();
        template.setInfo("Hello World !!");
        logger.info("Here is one of them printed out: " + gigaSpace.read(template));

        // wait 3000 ms:
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) { /*do nothing*/}

        int numInSpace = gigaSpace.count(template);
        logger.info("There are " + numInSpace + " Message objects in the space now.");
	}

}
