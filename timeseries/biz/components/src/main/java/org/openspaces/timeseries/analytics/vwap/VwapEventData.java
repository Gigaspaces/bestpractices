package org.openspaces.timeseries.analytics.vwap;

public class VwapEventData {
	private double vwap;
	private int period;
	public double getVwap() {
		return vwap;
	}
	public void setVwap(double vwap) {
		this.vwap = vwap;
	}
	public int getPeriod() {
		return period;
	}
	public void setPeriod(int period) {
		this.period = period;
	}
	

}
