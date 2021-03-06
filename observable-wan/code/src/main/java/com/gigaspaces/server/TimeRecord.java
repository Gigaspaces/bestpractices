package com.gigaspaces.server;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;

/**
 * TimeRecords keep track of timing info about other sites.
 * Each remote site will have a local TimeRecord instance.
 * The TimeRecord holds a subset of TimeStamp objects received
 * from remote sites; enough to calculate a meaningful 
 * continuous measurement of replication performance (10 by
 * default).
 * 
 * @author dfilppi
 *
 */
@SuppressWarnings("serial")
@SpaceClass
public class TimeRecord implements Serializable{
	private static Logger log=Logger.getLogger(TimeRecord.class.getName());
	private String location=null;
	private List<TimeEntry> timelog=null;
	private final int logsize=10;   //arbitrary

	//Only to be used for constructing templates
	public TimeRecord(){}
	
	public TimeRecord(String loc){
		this.location=loc;
		this.setTimelog(new LinkedList<TimeEntry>());
	}
	
	// Always want these in same partition
	@SpaceRouting
	public int getRoutingId(){
		return 1;
	}
	
	public void setRoutingId(int id){}
	
	public Long getTime() {
		if(getTimelog()==null)setTimelog(new LinkedList<TimeEntry>());
		if(getTimelog().size()==0)return null;
		return getTimelog().get(0).getTime();
	}
	public void setTime(TimeStamp ts) {
		if(getTimelog()==null)setTimelog(new LinkedList<TimeEntry>());
		if(getTimelog().size()>=logsize){
			getTimelog().remove(9);
		}
		getTimelog().add(0,new TimeEntry(ts));
		log.fine("setting time for loc="+location+" val="+getTimelog().get(0).getTime());
	}
	
	/**
	 * Computes average latency from the timelog.  
	 * @return
	 */
	public double getAveLatency(){
		if(getTimelog()==null)setTimelog(new LinkedList<TimeEntry>());
		if(getTimelog().size()==0)return 0;
		
		long totaltime=0;
		for(TimeEntry e:getTimelog()){
			totaltime+=e.getLatency();
		}
		return (double)(totaltime/((getTimelog().size())));
	}
	
	// Singleton per location
	@SpaceId
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	
	public String toString(){
		return String.format("{time=%d,loc=%s}", getTime(),location);
	}

	public int getSampleCount() {
		return getTimelog().size();
	}

	public void setTimelog(List<TimeEntry> timelog) {
		this.timelog = timelog;
	}

	public List<TimeEntry> getTimelog() {
		return timelog;
	}
}

class TimeEntry implements Serializable{
	private long time;
	private long latency;
	
	public TimeEntry(TimeStamp ts){
		time=ts.getTime();
		latency=System.currentTimeMillis()-ts.getTime();
	}
	
	public TimeEntry(long time, long latency) {
		this.time = time;
		this.latency = latency;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public long getLatency() {
		return latency;
	}
	public void setLatency(long latency) {
		this.latency = latency;
	}
}