package org.openspaces.timeseries.common;

import java.io.Serializable;

import com.gigaspaces.annotation.pojo.FifoSupport;
import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceIndex;
import com.gigaspaces.annotation.pojo.SpaceRouting;
import com.gigaspaces.metadata.index.SpaceIndexType;

@SuppressWarnings("serial")
@SpaceClass(fifoSupport=FifoSupport.OPERATION)
public class MarketDataEvent implements Serializable {
	/**
	 * 
	 */
	private String id=null;
	//Millisecond based time.  Possibly less granular depending on feed
	private Long eventTime=null;
	//The sequence # provided by the feed
	private Integer sequence=null;
	private String symbol=null;
	private TradeTick tradeData=null;
	private QuoteTick quoteData=null;
	private Object synthData=null;
	private String eventType=null;

	public MarketDataEvent(){
		
	}
	public MarketDataEvent(boolean isQuote) {
		if(isQuote){
			quoteData=new QuoteTick();
		}
		else{
			tradeData=new TradeTick();
		}
	}

	@SpaceIndex(type=SpaceIndexType.EXTENDED)
	public Long getEventTime() {
		return eventTime;
	}

	public String getId() {
		return id;
	}
	
	@SpaceId(autoGenerate=true)
	public void setId(String id) {
		this.id = id;
	}

	public String getSymbol() {
		return symbol;
	}

	@SpaceRouting
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public void setEventTime(Long time) {
		this.eventTime = time;
	}


	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public String toString(){
		return String.format("%05d %s %s", sequence,symbol,((quoteData!=null)?quoteData.toString():tradeData.toString()));
	}
	public TradeTick getTradeData() {
		return tradeData;
	}
	public QuoteTick getQuoteData() {
		return quoteData;
	}
	
	public void setTradeData(TradeTick tradeData) {
		this.tradeData = tradeData;
	}
	public void setQuoteData(QuoteTick quoteData) {
		this.quoteData = quoteData;
	}
	
	@SpaceIndex
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	
	
	public Object getSynthData() {
		return synthData;
	}
	public void setSynthData(Object synthData) {
		this.synthData = synthData;
	}



	public static class QuoteTick implements Serializable{

		private double bid;
		private double ask;
		private int bidvol;
		private int askvol;
		
		public int getBidvol() {
			return bidvol;
		}
		public void setBidvol(int bidvol) {
			this.bidvol = bidvol;
		}
		public int getAskvol() {
			return askvol;
		}
		public void setAskvol(int askvol) {
			this.askvol = askvol;
		}
		public double getBid() {
			return bid;
		}
		public void setBid(double bid) {
			this.bid = bid;
		}
		public double getAsk() {
			return ask;
		}
		public void setAsk(double ask) {
			this.ask = ask;
		}
		
		public String toString(){
			return String.format("Q bid=%-8.2d ask=%-8.2d bidvol=%d askvol=%d",bid,ask,bidvol,askvol);
		}
	}
	
	public static class TradeTick implements Serializable{
		
		private double price;
		private int volume;

		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			this.price = price;
		}

		public int getVolume() {
			return volume;
		}

		public void setVolume(int volume) {
			this.volume = volume;
		}
		
		public String toString(){
			return String.format("T pri=%-8.2f vol=%d",price,volume);
		}
		

	}

}
