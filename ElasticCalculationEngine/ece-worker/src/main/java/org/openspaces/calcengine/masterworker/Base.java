package org.openspaces.calcengine.masterworker;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceIndex;
import com.gigaspaces.annotation.pojo.SpaceProperty;
import com.gigaspaces.annotation.pojo.SpaceRouting;
import com.gigaspaces.annotation.pojo.SpaceProperty.IndexType;

@SpaceClass
public class Base {
	
	public Base (){}
	
	Integer jobID;
	String taskID;	
	Integer routing;

	@SpaceIndex
	public Integer getJobID() {
		return jobID;
	}
	public void setJobID(Integer jobID) {
		this.jobID = jobID;
	}
	
	@SpaceId(autoGenerate=false)
	public String getTaskID() {
		return taskID;
	}
	public void setTaskID(String taskID) {
		this.taskID = taskID;
	}

	@SpaceRouting
	public Integer getRouting() {
		return routing;
	}
	public void setRouting(Integer routing) {
		this.routing = routing;
	}
}
