package org.openspaces.timeseries.analytics.vwap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openspaces.timeseries.common.StringValProvider;

public class VwapEventData implements StringValProvider, Serializable {
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
	@Override
	public List<String> getStringVals() {
		List<String> l=new ArrayList<String>();
		l.add(String.valueOf(vwap));
		l.add(String.valueOf(period));
		return l;
	}
	
	public String toString(){
		return String.valueOf(vwap);
	}

}
