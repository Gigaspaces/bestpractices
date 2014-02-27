package com.mycompany.app.common.vo;

import java.util.SortedMap;
import java.util.TreeMap;

public class Airline implements java.io.Serializable  {

	private static final long serialVersionUID = 448524636205229981L;
	
	private String airlineName;
	private SortedMap<Integer, Interval> intervals;
	
	public Airline() {}
	
	public Airline(String airlineName) {
		this.airlineName = airlineName;
	}
	
	public Airline(String airlineName, SortedMap<Integer, Interval> intervals) {
		this.airlineName = airlineName;
		this.intervals = intervals;
	}
	
	public String getAirlineName() {
		return airlineName;
	}
	
	public void setAirlineName(String airlineName) {
		this.airlineName = airlineName;
	}

	public SortedMap<Integer, Interval> getIntervals() {
		if(intervals == null)
			intervals = new TreeMap<Integer, Interval>();
		return intervals;
	}

	public void setIntervals(SortedMap<Integer, Interval> intervals) {
		this.intervals = intervals;
	}	
	
}