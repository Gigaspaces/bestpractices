package com.mycompany.app.common.domain;

import com.mycompany.app.common.utils.Constants;

public class BookingTimeSeries extends TimeSeries {
	
	private static final long serialVersionUID = 4990920950730003971L;
	
	private String type = Constants.TYPE_BOOKING;
	
	public BookingTimeSeries() {}
	
	public BookingTimeSeries(Integer interval, String airline) {
		this.intervalId = TimeSeries.createIntervalId(interval, airline);
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