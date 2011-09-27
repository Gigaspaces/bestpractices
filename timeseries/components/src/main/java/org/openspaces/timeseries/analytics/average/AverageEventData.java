package org.openspaces.timeseries.analytics.average;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openspaces.timeseries.common.StringValProvider;

public class AverageEventData implements StringValProvider,Serializable{
	private double average;
	private int period;
	
	public double getAverage() {
		return average;
	}
	public void setAverage(double average) {
		this.average = average;
	}
	public int getPeriod() {
		return period;
	}
	public void setPeriod(int period) {
		this.period = period;
	}
	@Override
	public List<String> getStringVals() {
		List<String> l=new ArrayList<String>();
		l.add(String.valueOf(average));
		l.add(String.valueOf(period));
		return l;
	}
	public String toString(){
		return String.valueOf(average);
	}
	
}
