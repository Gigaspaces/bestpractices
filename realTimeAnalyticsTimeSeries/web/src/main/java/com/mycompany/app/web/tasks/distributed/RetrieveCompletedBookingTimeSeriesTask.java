package com.mycompany.app.web.tasks.distributed;

import java.util.List;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.openspaces.core.executor.AutowireTask;
import org.openspaces.core.executor.DistributedTask;

import com.gigaspaces.annotation.pojo.SpaceRouting;
import com.gigaspaces.async.AsyncResult;
import com.mycompany.app.common.dao.IBookingTimeSeriesDAO;
import com.mycompany.app.common.domain.BookingTimeSeries;
import com.mycompany.app.common.utils.AirportDataUtils;
import com.mycompany.app.common.vo.SourceDestinationAirports;


@AutowireTask
public class RetrieveCompletedBookingTimeSeriesTask implements DistributedTask<BookingTimeSeries[], TreeMap<String, SourceDestinationAirports>> {

	private static final long serialVersionUID = -2846168106076276015L;
	
	private String airline;
	private Integer lastInterval;
    
	@Resource
	private transient IBookingTimeSeriesDAO bookingTimeSeriesDAO;
	
	public RetrieveCompletedBookingTimeSeriesTask() {};
	
    public RetrieveCompletedBookingTimeSeriesTask(String airline, Integer lastInterval) {
		this.airline = airline;
		this.lastInterval = lastInterval;
	}
    
    @SpaceRouting
    public String getAirline() {
		return airline;
    }
    
    public BookingTimeSeries[] execute() throws Exception {
		return bookingTimeSeriesDAO.readAllCompletedTimeSeriesAfterInterval(lastInterval);
    }
    
    public TreeMap<String, SourceDestinationAirports> reduce(List<AsyncResult<BookingTimeSeries[]>> results) throws Exception {
    	TreeMap<String, SourceDestinationAirports> airports = new TreeMap<String, SourceDestinationAirports>();
		
    	if(results != null) {
			for(AsyncResult<BookingTimeSeries[]> result: results) {
				if(result != null) { 
					if(result.getException() != null) {
		                throw result.getException();
		            }
					
					for(BookingTimeSeries bookingTimeSeries : result.getResult()) {
						if(bookingTimeSeries != null) {
							AirportDataUtils.updateAirportMap(bookingTimeSeries, airports);
						}
					}
				}
			}
    	}
		
		return airports;
    }

	public IBookingTimeSeriesDAO getBookingTimeSeriesDAO() {
		return bookingTimeSeriesDAO;
	}

	public void setBookingTimeSeriesDAO(IBookingTimeSeriesDAO bookingTimeSeriesDAO) {
		this.bookingTimeSeriesDAO = bookingTimeSeriesDAO;
	}
	
}