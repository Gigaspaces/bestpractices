package com.mycompany.app.common.domain;

import com.mycompany.app.common.utils.Constants;

public class SearchTimeSeries extends TimeSeries {
	
	private static final long serialVersionUID = -8199567513974196423L;
	
	private String type = Constants.TYPE_SEARCH;
	
	public SearchTimeSeries() {}
	
	public SearchTimeSeries(Integer interval, String airline) {
		this.intervalId = interval + Constants.UNDER_SCORE + airline;
		this.interval = interval;
		this.airline = airline;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}