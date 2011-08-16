package com.eauction.gigaspaces.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openspaces.pu.container.standalone.StandaloneContainerRunnable;

import java.util.LinkedList;
import java.util.List;


/**
 * This class demonstrates a way of creating your own processing unit container.
 *
 * @author jeroen
 */
public class UserWriterStarter {
    private static final Log log = LogFactory.getLog(UserWriterStarter.class);

    public static void main(String[] args) {
        List<String> configs = new LinkedList<String>();
        configs.add("classpath:/META-INF/spring/pu.xml");

        final StandaloneContainerRunnable runnable = new StandaloneContainerRunnable(null,
                null, configs);

        // If you wish, you can start your own container in its own thread 
        new Thread() {
                public void run() {
                    log.info("Starting the container");
                    runnable.run();
                }
            }.start();

        // Wait for the container is still initializing.
        while (!runnable.isInitialized()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }

        // Report any exceptions that occurred.
        if (runnable.hasException()) {
            log.error(runnable.getException().getMessage(),
                runnable.getException());
        }

        // Since all the work of the UserWriter was done in @PostConstruct, it is done, so shutdown.

        // Stop the container gracefully`
        log.info("Stopping the container");
        runnable.stop();

        /*
         * Alternatively you can create a container like below.
         * The first parameter to the constructor denotes the location of the PU to start
        StandaloneProcessingUnitContainerProvider provider = new StandaloneProcessingUnitContainerProvider(".");
        
        provider.addConfigLocation("classpath:/META-INF/spring/pu.xml");
        
        ProcessingUnitContainer container = provider.createContainer();
        container.close();
        */
    }
}
