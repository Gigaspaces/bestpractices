package com.mycompany.app.feeder;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

public abstract class Feeder {

	protected Logger log = Logger.getLogger(this.getClass().getName());
    
    protected ScheduledExecutorService executorService;
    protected ScheduledFuture<?> sf;
    
    protected long defaultDelay = 100;

	public void setDefaultDelay(long defaultDelay) {
		this.defaultDelay = defaultDelay;
	}
		
}