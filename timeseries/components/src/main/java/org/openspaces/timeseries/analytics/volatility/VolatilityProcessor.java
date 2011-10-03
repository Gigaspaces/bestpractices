package org.openspaces.timeseries.analytics.volatility;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.openspaces.timeseries.analytics.AnalyticBase;
import org.openspaces.timeseries.common.MarketDataEvent;
import org.openspaces.timeseries.processor.PeriodTimerEvent;
import org.openspaces.timeseries.processor.PeriodTimerEventProcessor;

import com.j_spaces.core.client.ReadModifiers;
import com.j_spaces.core.client.SQLQuery;

/* Just trades for now*/
public class VolatilityProcessor extends PeriodTimerEventProcessor implements AnalyticBase {
	static Logger log=Logger.getLogger(VolatilityProcessor.class.getName());
	SQLQuery<MarketDataEvent> query=new SQLQuery<MarketDataEvent>(MarketDataEvent.class,"eventType = 'trade' and eventTime >= ? and eventTime < ?");

	@Override
	public String getName() {
		return "volatility";
	}

	@Override
	protected PeriodTimerEvent processEvent(PeriodTimerEvent event) {
		
		long end=event.getTime();
		query.setParameters(end-getInterval()*1000L,end);
		
		MarketDataEvent[] ticks=gigaSpace.readMultiple(query,Integer.MAX_VALUE,ReadModifiers.FIFO);
		
		if(ticks.length==0)return null;

		Map<String,VolData> calcdata=new HashMap<String,VolData>();

		for(MarketDataEvent tick:ticks){
			VolData data=calcdata.get(tick.getSymbol());
			if(data==null){
				data=new VolData();
				data.symbol=tick.getSymbol();
				calcdata.put(data.symbol,data);
			}
			data.total=data.total+tick.getTradeData().getPrice();
			data.numticks++;
		}
		
		for(MarketDataEvent tick:ticks){
			VolData data=calcdata.get(tick.getSymbol());
			double dev=tick.getTradeData().getPrice()-(data.total/data.numticks);
			data.devsum=data.devsum+(dev*dev);
		}
		
		//Create synthetic events
		for(VolData vd:calcdata.values()){
			MarketDataEvent newevent=new MarketDataEvent();
			newevent.setEventTime(event.getTime());
			newevent.setEventType(getName());
			newevent.setSymbol(vd.symbol);
			VolatilityEventData ved=new VolatilityEventData();
			ved.setVolatility(Math.sqrt(vd.devsum/(vd.total/vd.numticks)));
			ved.setPeriod(event.getInterval());
			newevent.setSynthData(ved);
			log.info("data:"+ved.toString());
			gigaSpace.write(newevent);
		}
		return null;
	}
	
	class VolData{
		public String symbol;
		public int numticks;
		public double total;
		public double devsum;
	}
}


