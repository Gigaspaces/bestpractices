package org.openspaces.timeseries.processor;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.events.EventDriven;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.notify.Notify;

@EventDriven @Notify
public abstract class PeriodTimerEventProcessor {
	protected int interval;
	private PeriodTimer timer;

	@GigaSpaceContext(name = "gigaSpace")
	protected GigaSpace gigaSpace;

	/*
	 * Subclasses override to provide functionality
	 */
	protected abstract PeriodTimerEvent processEvent(PeriodTimerEvent event);
	
	@EventTemplate
	public PeriodTimerEvent template() throws Exception{
		if(interval==0)throw new Exception("interval not set");
		PeriodTimerEvent template=new PeriodTimerEvent();
		template.setInterval(interval);
		return template;
	}
	
	@SpaceDataEvent
	public PeriodTimerEvent onEvent(PeriodTimerEvent event) {
		return processEvent(event);
	}
	
	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
		if(timer!=null)timer.addPeriod(interval);
	}

	public PeriodTimer getTimer() {
		return timer;
	}

	public void setTimer(PeriodTimer timer)  {
		this.timer = timer;
		if(interval!=0)timer.addPeriod(interval);
	}

}
