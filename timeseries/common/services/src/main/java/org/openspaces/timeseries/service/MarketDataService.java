package org.openspaces.timeseries.service;

import org.openspaces.remoting.Routing;
import org.openspaces.timeseries.common.MarketDataEvent;



public interface MarketDataService {

	
	MarketDataEvent[] getTicks(@Routing String symbol, String tickType, Long starttime, Long endtime);
	
	
}
