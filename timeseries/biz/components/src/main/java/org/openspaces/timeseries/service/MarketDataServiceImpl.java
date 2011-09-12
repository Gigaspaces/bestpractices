package org.openspaces.timeseries.service;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.remoting.RemotingService;
import org.openspaces.timeseries.common.MarketDataEvent;

import com.j_spaces.core.client.SQLQuery;

@RemotingService
public class MarketDataServiceImpl implements MarketDataService {

	@GigaSpaceContext(name = "gigaSpace")
    transient GigaSpace gigaSpace;

	@Override
	public MarketDataEvent[] getTicks(String symbol, String tickType,Long starttime, Long endtime) {
		SQLQuery<MarketDataEvent> q=new SQLQuery<MarketDataEvent>(MarketDataEvent.class,"eventType = ? and eventTime >= ? and eventTime < ?",tickType,starttime,endtime);
		
		return gigaSpace.readMultiple(q, Integer.MAX_VALUE);
	}

}
