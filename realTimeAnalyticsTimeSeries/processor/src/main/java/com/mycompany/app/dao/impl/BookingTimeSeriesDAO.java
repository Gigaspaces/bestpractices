package com.mycompany.app.dao.impl;

import java.util.Date;

import com.gigaspaces.client.ChangeSet;
import com.gigaspaces.query.IdQuery;
import com.j_spaces.core.client.SQLQuery;
import com.mycompany.app.common.dao.IBookingTimeSeriesDAO;
import com.mycompany.app.common.domain.BookingTimeSeries;
import com.mycompany.app.common.domain.TimeSeries;
import com.mycompany.app.common.utils.Constants;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;


public class BookingTimeSeriesDAO implements IBookingTimeSeriesDAO {

	@GigaSpaceContext(name = "gigaSpace")
	private GigaSpace gigaSpace;
	
	public void setGigaSpace(GigaSpace gigaSpace) {
		this.gigaSpace = gigaSpace;
	}
	
	public BookingTimeSeries[] readAllCompletedTimeSeriesAfterInterval(Integer lastInterval) {
		SQLQuery<BookingTimeSeries> query = new SQLQuery<BookingTimeSeries>(BookingTimeSeries.class, "interval > ? and status = ?");
		query.setParameter(1, lastInterval);
        query.setParameter(2, Constants.STATUS_COMPLETE);
		
		return gigaSpace.readMultiple(query);
	}
	
	public BookingTimeSeries readActiveTimeSeriesByAirline(String airline) {
		SQLQuery<BookingTimeSeries> query = new SQLQuery<BookingTimeSeries>(BookingTimeSeries.class, "airline = ? and status = ?")
		.setProjections("interval"); //Use Projections API to return only the interval attribute 
        
		query.setParameter(1, airline);
        query.setParameter(2, Constants.STATUS_ACTIVE);
        
        return gigaSpace.read(query);
	}
	
	public BookingTimeSeries readTimeSeriesByIntervalId(Integer interval, String airline) {
		String intervalId = TimeSeries.createIntervalId(interval, airline);
		return gigaSpace.readById(BookingTimeSeries.class, intervalId, airline);
	}
	
	//BookingTimeSeries will only remain in the space for 60 seconds which correlates to approximately 35-40 intervals
	public void save(BookingTimeSeries bookingTimeSeries) {
		gigaSpace.write(bookingTimeSeries, 60000);
	}
	
	public BookingTimeSeries findBookingTimeSeriesWithinActiveInterval(String airline, long timeSeriesInterval) {
		SQLQuery<BookingTimeSeries> query = new SQLQuery<BookingTimeSeries>(BookingTimeSeries.class, "airline = ? and lasttimestamp > ?");
        query.setParameter(1, airline);
        query.setParameter(2, new Date(System.currentTimeMillis() - timeSeriesInterval));
        
        return gigaSpace.read(query);
	}
	
	public void incrementSourceDestinationCounter(BookingTimeSeries bookingTimeSeries, String sourceDestination) {
		IdQuery<BookingTimeSeries> idQuery = new IdQuery<BookingTimeSeries>(BookingTimeSeries.class, bookingTimeSeries.getIntervalId());
		gigaSpace.change(idQuery, new ChangeSet().increment("sourceDestinationCounter." + sourceDestination, 1));
	}
	
	public void updateCompletedStatus(BookingTimeSeries bookingTimeSeries) {
		IdQuery<BookingTimeSeries> idQuery = new IdQuery<BookingTimeSeries>(BookingTimeSeries.class, bookingTimeSeries.getIntervalId());
		gigaSpace.change(idQuery, new ChangeSet().set("status", Constants.STATUS_COMPLETE));
	}
	
}