package org.openspaces.timeseries.service;

import org.openspaces.remoting.Routing;



public interface MarketDataService {
	
	int getTotalTicks();

	String[][] getTicks(@Routing String symbol, String[] tickTypes, Long starttime, Long endtime);
	
	void clear();
}
