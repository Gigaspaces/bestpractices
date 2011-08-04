package org.openspaces.calcengine.masterworker;

import com.gigaspaces.annotation.pojo.SpaceClass;

@SpaceClass
public class Request extends Base{
	public Request (){}
	
	Integer[] tradeIds ;
	Double rate ;
	
	public Integer[] getTradeIds() {
		return tradeIds;
	}
	public void setTradeIds(Integer[] tradeIds) {
		this.tradeIds = tradeIds;
	}
	public Double getRate() {
		return rate;
	}
	public void setRate(Double rate) {
		this.rate = rate;
	}
}
