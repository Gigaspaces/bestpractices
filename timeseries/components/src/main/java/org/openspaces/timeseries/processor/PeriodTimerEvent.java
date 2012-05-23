package org.openspaces.timeseries.processor;

import com.gigaspaces.annotation.pojo.FifoSupport;
import com.gigaspaces.annotation.pojo.SpaceClass;

@SpaceClass(fifoSupport=FifoSupport.OPERATION)
public class PeriodTimerEvent {
	private Integer interval;
	private Long time;
	
	public PeriodTimerEvent(){}
	
	public PeriodTimerEvent(long time,int interval){
		setTime(time);
		setInterval(interval);
	}

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}
	
	public String toString(){
		return String.format("time=%d interval=%d",time,interval);
	}
}
