package org.openspaces.timeseries.analytics.volume;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openspaces.timeseries.common.StringValProvider;

public class VolumeEventData implements Serializable,StringValProvider {
	private int volume;
	private int period;
	
	public double getVolume() {
		return volume;
	}
	public void setVolume(int volume) {
		this.volume = volume;
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
		l.add(String.valueOf(volume));
		l.add(String.valueOf(period));
		return l;
	}

	public String toString(){
		return String.valueOf(volume);
	}
	
}
