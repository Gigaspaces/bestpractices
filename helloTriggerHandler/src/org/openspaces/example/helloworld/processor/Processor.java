package org.openspaces.example.helloworld.processor;

import java.util.logging.Logger;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceLateContext;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.example.helloworld.common.Message;


/**
 * The processor is passed interesting Objects from its associated PollingContainer
 * <p>The PollingContainer removes objects from the GigaSpace that match the criteria
 * specified for it.
 * <p>Once the Processor receives each Object, it modifies its state and returns it to
 * the PollingContainer which writes them back to the GigaSpace
 * <p/>
 * <p>The PollingContainer is configured in the pu.xml file of this project
 */
public class Processor {
    Logger logger=Logger.getLogger(this.getClass().getName());

    /**
     * Process the given Message and return it to the caller.
     * This method is invoked using OpenSpaces Events when a matching event
     * occurs.
     */
    @SpaceDataEvent
    public Message processMessage(Message msg) {
    	
        logger.info("Processor PROCESSING: " + msg);
        msg.setInfo(msg.getInfo() + "World !!");
        msg.setProcessed(true);
        
        try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return msg;
    }

    public Processor() {
        logger.info("Processor instantiated, waiting for messages feed...");
    }

}
