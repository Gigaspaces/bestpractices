package org.openspaces.timeseries.analytics.average;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.openspaces.timeseries.analytics.AnalyticBase;
import org.openspaces.timeseries.common.MarketDataEvent;
import org.openspaces.timeseries.processor.PeriodTimerEvent;
import org.openspaces.timeseries.processor.PeriodTimerEventProcessor;

import com.j_spaces.core.client.ReadModifiers;
import com.j_spaces.core.client.SQLQuery;

public class AverageProcessor extends PeriodTimerEventProcessor implements AnalyticBase{
	static Logger log=Logger.getLogger(AverageProcessor.class.getName());
	SQLQuery<MarketDataEvent> query=new SQLQuery<MarketDataEvent>(MarketDataEvent.class,"eventType = 'trade' and eventTime >= ? and eventTime < ?");

	@Override
	public String getName() {
		return "average";
	}

	@Override
	protected PeriodTimerEvent processEvent(PeriodTimerEvent event) {
		
		long end=event.getTime();
		query.setParameters(end-getInterval()*1000L,end);
		
		MarketDataEvent[] ticks=gigaSpace.readMultiple(query,Integer.MAX_VALUE,ReadModifiers.FIFO);
		
		if(ticks.length==0)return null;

		Map<String,AveData> calcdata=new HashMap<String,AveData>();

		for(MarketDataEvent tick:ticks){
			AveData data=calcdata.get(tick.getSymbol());
			if(data==null){
				data=new AveData();
				data.symbol=tick.getSymbol();
				calcdata.put(data.symbol,data);
			}
			data.tickcnt++;
			data.pricecum+=(tick.getTradeData().getPrice());
		}
		
		//Create synthetic events
		for(AveData vd:calcdata.values()){
			MarketDataEvent newevent=new MarketDataEvent();
			newevent.setEventTime(event.getTime());
			newevent.setEventType(getName());
			newevent.setSymbol(vd.symbol);
			AverageEventData ved=new AverageEventData();
			ved.setAverage(vd.pricecum/vd.tickcnt);
			ved.setPeriod(event.getInterval());
			newevent.setSynthData(ved);
			log.info("data:"+ved.toString());
			gigaSpace.write(newevent);
		}
		return null;
	}
	
	class AveData{
		public String symbol;
		public int tickcnt;
		public double pricecum;
	}
	

}
