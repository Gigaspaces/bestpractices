package com.mycompany.app.common.dao;

import com.mycompany.app.common.domain.BookingTimeSeries;

public interface IBookingTimeSeriesDAO {
	
	BookingTimeSeries[] readAllCompletedTimeSeriesAfterInterval(Integer lastInterval);
	
	BookingTimeSeries readTimeSeriesByIntervalId(Integer interval, String airline);
	
	BookingTimeSeries readActiveTimeSeriesByAirline(String airline);
	
	void save(BookingTimeSeries bookingTimeSeries);
	
	BookingTimeSeries findBookingTimeSeriesWithinActiveInterval(String airline, long timeSeriesInterval);
		
	void incrementSourceDestinationCounter(BookingTimeSeries bookingTimeSeries, String sourceDestination);

	void updateCompletedStatus(BookingTimeSeries bookingTimeSeries);
}