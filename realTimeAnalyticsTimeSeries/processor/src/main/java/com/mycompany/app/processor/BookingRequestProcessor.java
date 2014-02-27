package com.mycompany.app.processor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;
import org.springframework.util.Assert;

import com.mycompany.app.common.dao.IBookingTimeSeriesDAO;
import com.mycompany.app.common.domain.BookingRequest;
import com.mycompany.app.common.domain.BookingTimeSeries;
import com.mycompany.app.common.utils.AirportDataUtils;
import com.mycompany.app.common.utils.Constants;

/**
 * The processor simulates work done on unprocessed BookingRequest objects. The processData accepts a BookingRequest object,
 * simulates work by sleeping, and then sets the processed flag to true and returns the processed BookingRequest.
 */
@Polling(concurrentConsumers=10)
public class BookingRequestProcessor {
	
    private Logger log = Logger.getLogger(this.getClass().getName());

    private long timeSeriesInterval;    
    private Map<String, Integer> intervalAirlineMap = new HashMap<String, Integer>(4);
    
    private IBookingTimeSeriesDAO bookingTimeSeriesDAO;
    
	
    @SpaceDataEvent
    public void processData(BookingRequest bookingRequest) {
    	Assert.notNull(bookingTimeSeriesDAO, "**** bookingTimeSeriesDAO is a required property ****");
    	
    	bookingRequest.setProcessedData("PROCESSED : " + bookingRequest.getRawData());
    	bookingRequest.setProcessed(true);
    	String sourceDestinationAirport = AirportDataUtils.generateSourceDestinationKey(bookingRequest.getSourceAirport(), bookingRequest.getDestinationAirport());
    	//log.info(" ------ BOOKING REQUEST PROCESSED : " + bookingRequest);
                
        BookingTimeSeries result = bookingTimeSeriesDAO.findBookingTimeSeriesWithinActiveInterval(bookingRequest.getAirline(), timeSeriesInterval);        
        if(result != null)
    		bookingTimeSeriesDAO.incrementSourceDestinationCounter(result, sourceDestinationAirport);
    	else
    		createNewInterval(bookingRequest.getAirline(), sourceDestinationAirport);
    }

	private void createNewInterval(String airline, String sourceDestination) {
    	Integer interval  = intervalAirlineMap.get(airline);
		if(interval != null)
			intervalAirlineMap.put(airline, ++interval);
		else {
			interval = 1;
			intervalAirlineMap.put(airline, interval);
		}
		    		
    	Map<String, Integer> sourceDestinationCounterMap = new HashMap<String, Integer>();
    	sourceDestinationCounterMap.put(sourceDestination, 1);
    	
		BookingTimeSeries bookingTimeSeries = new BookingTimeSeries(interval, airline);
    	bookingTimeSeries.setSourceDestinationCounter(sourceDestinationCounterMap);
    	bookingTimeSeries.setLasttimestamp(new Date(System.currentTimeMillis()));
    	bookingTimeSeries.setStatus(Constants.STATUS_ACTIVE);
    	bookingTimeSeriesDAO.save(bookingTimeSeries);
    	
    	updatePreviousIntervalStatusAsCompleted(airline, interval-1);
    }
	
	private void updatePreviousIntervalStatusAsCompleted(String airline, Integer interval) {
		BookingTimeSeries bookingTimeSeries = bookingTimeSeriesDAO.readTimeSeriesByIntervalId(interval, airline);        
        if(bookingTimeSeries != null) {
        	bookingTimeSeriesDAO.updateCompletedStatus(bookingTimeSeries);
        }
	}
	
    public void setTimeSeriesInterval(long timeSeriesInterval) {
		this.timeSeriesInterval = timeSeriesInterval;
	}

	public void setBookingTimeSeriesDAO(IBookingTimeSeriesDAO bookingTimeSeriesDAO) {
		this.bookingTimeSeriesDAO = bookingTimeSeriesDAO;
	}

}