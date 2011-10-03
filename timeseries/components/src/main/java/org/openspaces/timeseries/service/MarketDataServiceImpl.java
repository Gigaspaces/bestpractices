package org.openspaces.timeseries.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.remoting.RemotingService;
import org.openspaces.timeseries.common.MarketDataEvent;

import com.j_spaces.core.client.SQLQuery;

@RemotingService(exporter="serviceExporter")
public class MarketDataServiceImpl implements MarketDataService {
	static Logger log=Logger.getLogger(MarketDataServiceImpl.class.getName());
	@GigaSpaceContext(name = "gigaSpace")
    transient GigaSpace gigaSpace;
	
	/**
	 * Return value is not user objects because easier with gwt this way. Not a production pattern.
	 */
	@Override
	public String[][] getTicks(String symbol, String[] tickTypes,Long starttime, Long endtime) {
		SQLQuery<MarketDataEvent> q=null;
		if(tickTypes==null){
			q=new SQLQuery<MarketDataEvent>(MarketDataEvent.class,"eventTime >= ? and eventTime < ?",starttime,endtime);
		}
		else{
			StringBuilder sb=new StringBuilder();
			sb.append("eventType in (");
			for(String tt:tickTypes){
				sb.append("'").append(tt).append("'").append(",");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append(")");
			q=new SQLQuery<MarketDataEvent>(MarketDataEvent.class,sb.toString()+" and eventTime >= ? and eventTime < ?",starttime,endtime);
		}
		log.info("getTicks query :"+q.getQuery());
		log.info(String.format("timerange %d %d",starttime,endtime));
		
		MarketDataEvent[] events= gigaSpace.readMultiple(q, Integer.MAX_VALUE);
		List<String[]> retlist=new ArrayList<String[]>();
		for(MarketDataEvent e:events){
			String[] val=new String[3+e.getStringVals().size()];
			val[0]=e.getSymbol();
			val[1]=e.getEventType();
			val[2]=e.getEventTime().toString();
			int i=3;
			for(String vv:e.getStringVals()){
				val[i++]=vv;
			}
			retlist.add(val);
		}
		log.info("getTicks return count :"+retlist.size());
		return retlist.toArray(new String[][]{});
	}

	@Override
	public int getTotalTicks() {
		return gigaSpace.count(new MarketDataEvent());
	}

	@Override
	public void clear() {
		gigaSpace.clean();
	}

}

