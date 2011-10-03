package org.openspaces.timeseries.analytics.volatility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openspaces.timeseries.common.StringValProvider;

import com.gigaspaces.annotation.pojo.SpaceExclude;

public class VolatilityEventData implements Serializable,StringValProvider {
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
	@Override
	public List<String> getStringVals() {
		List<String> l=new ArrayList<String>();
		l.add(String.valueOf(volatility));
		l.add(String.valueOf(period));
		return l;
	}

	public String toString(){
		return String.valueOf(volatility);
	}
	
}
