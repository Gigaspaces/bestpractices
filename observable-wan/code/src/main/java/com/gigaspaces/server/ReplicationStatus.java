package com.gigaspaces.server;

import java.io.Serializable;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;

/**
 * This singleton class holds a simple (perhaps too simple) representation
 * of the replication performance at a given site.  The ReplicationMonitor
 * periodically polls the various TimeRecord objects representing each 
 * remote site, and boils it down to a simple status value.  This object
 * can then be monitored for changes to update a UI or set off an alert.
 * 
 * @author dfilppi
 *
 */
@SuppressWarnings("serial")
@SpaceClass
public class ReplicationStatus implements Serializable {
	private Status status=null;
	
	public static enum Status{
		UP, DEGRADED, DOWN 
	}

	@SpaceId
	public int getId(){
		return 0;
	}
	
	public void setId(int id){
	}
	
	@SpaceRouting
	public int getRoutingId(){
		return 1;
	}
	
	public void setRoutingId(int id){}
	

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
}
