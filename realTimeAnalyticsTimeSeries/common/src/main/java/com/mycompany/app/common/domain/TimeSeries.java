package com.mycompany.app.common.domain;

import java.util.Date;
import java.util.Map;

import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceIndex;
import com.gigaspaces.annotation.pojo.SpaceRouting;
import com.gigaspaces.metadata.index.SpaceIndexType;
import com.mycompany.app.common.utils.Constants;

public abstract class TimeSeries implements java.io.Serializable {
	
	private static final long serialVersionUID = -2128516066057590442L;
	
	Integer interval;
	String intervalId;
	String airline;
	Date lasttimestamp;
	Map<String, Integer> sourceDestinationCounter;
	String status;
	
	public static final String createIntervalId(Integer interval, String airline) {
		return interval + Constants.UNDER_SCORE + airline;
	}
	
	public TimeSeries() {
	}
	
	@SpaceId(autoGenerate=false)
	public String getIntervalId() {
		return intervalId;
	}
	
	@SpaceRouting
	@SpaceIndex(type=SpaceIndexType.BASIC)
	public String getAirline() {
		return airline;
	}
	
	@SpaceIndex(type=SpaceIndexType.EXTENDED)
	public Date getLasttimestamp() {
		return lasttimestamp;
	}
	
	public Map<String, Integer> getSourceDestinationCounter() {
		return sourceDestinationCounter;
	}

	public void setSourceDestinationCounter(Map<String, Integer> sourceDestinationCounter) {
		this.sourceDestinationCounter = sourceDestinationCounter;
	}

	public void setAirline(String airline) {
		this.airline = airline;
	}

	public void setLasttimestamp(Date lasttimestamp) {
		this.lasttimestamp = lasttimestamp;
	}

	public void setIntervalId(String intervalId) {
		this.intervalId = intervalId;
	}

	@SpaceIndex(type=SpaceIndexType.EXTENDED)
	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	@SpaceIndex(type=SpaceIndexType.BASIC)
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}