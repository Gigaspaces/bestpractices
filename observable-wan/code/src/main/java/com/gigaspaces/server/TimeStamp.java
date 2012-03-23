package com.gigaspaces.server;

import java.io.Serializable;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceRouting;

/**
 * TimeStamps are written on a regular basis at a given site, with
 * the intent that they get replicated to other sites.  TimeStamps are
 * the only artifacts of the WAN observation mechanism that get
 * replicated.  Timestamps trigger TimeWatchers at other sites, and
 * then are immediately deleted.
 * 
 * @author DeWayne
 *
 */
@SuppressWarnings("serial")
@SpaceClass
public class TimeStamp implements Serializable{
	private Long time;
	private String location;
	
	public TimeStamp(){
	}
	
	public TimeStamp(String loc){
		this.location=loc;
		this.time=System.currentTimeMillis();
	}
	
	// Always want these in same partition
	@SpaceRouting
	public int getRoutingId(){
		return 1;
	}
	
	public void setRoutingId(int id){}
	
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	
	public String toString(){
		return String.format("{time=%d,loc=%s}", time,location);
	}
	

}
