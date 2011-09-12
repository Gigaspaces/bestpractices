package org.openspaces.timeseries.analytics.vwap;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.timeseries.analytics.AnalyticBase;
import org.openspaces.timeseries.analytics.volatility.VolatilityEventData;
import org.openspaces.timeseries.common.MarketDataEvent;
import org.openspaces.timeseries.processor.PeriodTimerEvent;
import org.openspaces.timeseries.processor.PeriodTimerEventProcessor;

import com.j_spaces.core.client.ReadModifiers;
import com.j_spaces.core.client.SQLQuery;

public class 
VwapProcessor extends PeriodTimerEventProcessor implements AnalyticBase{
	SQLQuery<MarketDataEvent> query=new SQLQuery<MarketDataEvent>(MarketDataEvent.class,"eventType = 'trade' and eventTime >= ? and eventTime < ?");

	@Override
	public String getName() {
		return "vwap";
	}

	@Override
	protected PeriodTimerEvent processEvent(PeriodTimerEvent event) {
		
		long end=event.getTime();
		query.setParameters(end-getInterval()*1000L,end);
		
		MarketDataEvent[] ticks=gigaSpace.readMultiple(query,Integer.MAX_VALUE,ReadModifiers.FIFO);
		
		if(ticks.length==0)return null;

		Map<String,VwapData> calcdata=new HashMap<String,VwapData>();

		for(MarketDataEvent tick:ticks){
			VwapData data=calcdata.get(tick.getSymbol());
			if(data==null){
				data=new VwapData();
				data.symbol=tick.getSymbol();
				calcdata.put(data.symbol,data);
			}
			data.totalvol=data.totalvol+tick.getTradeData().getVolume();
			data.pricecum=data.pricecum+(tick.getTradeData().getPrice()*(double)tick.getTradeData().getVolume());
		}
		
		//Create synthetic events
		for(VwapData vd:calcdata.values()){
			MarketDataEvent newevent=new MarketDataEvent();
			newevent.setEventTime(event.getTime());
			newevent.setEventType(getName());
			newevent.setSymbol(vd.symbol);
			VwapEventData ved=new VwapEventData();
			ved.setVwap(vd.pricecum/vd.totalvol);
			ved.setPeriod(event.getInterval());
			newevent.setSynthData(ved);
			gigaSpace.write(newevent);
		}
		return null;
	}
	
	class VwapData{
		public String symbol;
		public int totalvol;
		public double pricecum;
	}
	

}
