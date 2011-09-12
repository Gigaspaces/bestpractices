package org.openspaces.timeseries.analytics.average;

public class AverageEventData {
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
	
}
