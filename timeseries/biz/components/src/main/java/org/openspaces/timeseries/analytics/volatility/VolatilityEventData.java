package org.openspaces.timeseries.analytics.volatility;

public class VolatilityEventData {
	private double volatility;
	private int period;
	public double getVolatility() {
		return volatility;
	}
	public void setVolatility(double volatility) {
		this.volatility = volatility;
	}
	public int getPeriod() {
		return period;
	}
	public void setPeriod(int period) {
		this.period = period;
	}
	
	
}
