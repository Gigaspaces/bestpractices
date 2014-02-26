package com.mycompany.app.web.tasks;

import java.util.TreeMap;

import javax.annotation.Resource;

import org.openspaces.core.executor.AutowireTask;
import org.openspaces.core.executor.Task;

import com.gigaspaces.annotation.pojo.SpaceRouting;
import com.mycompany.app.common.dao.IBookingTimeSeriesDAO;
import com.mycompany.app.common.domain.BookingTimeSeries;
import com.mycompany.app.common.utils.AirportDataUtils;
import com.mycompany.app.common.vo.SourceDestinationAirports;

@AutowireTask
public class RetrieveNextActiveBookingTimeSeriesTask implements Task<TreeMap<String, SourceDestinationAirports>> {

	private static final long serialVersionUID = -2846168106076276015L;
	
	private String airline;
	private Integer lastInterval;
    
	@Resource
	private transient IBookingTimeSeriesDAO bookingTimeSeriesDAO;
	
	public RetrieveNextActiveBookingTimeSeriesTask() {};
	
    public RetrieveNextActiveBookingTimeSeriesTask(String airline, Integer lastInterval) {
		this.airline = airline;
		this.lastInterval = lastInterval;
	}
    
    @SpaceRouting
    public String getAirline() {
		return airline;
    }
    
    public TreeMap<String, SourceDestinationAirports> execute() throws Exception {    	
    	BookingTimeSeries bookingTimeSeries = bookingTimeSeriesDAO.readTimeSeriesByIntervalId(lastInterval+1, airline);
    	
		//Reuse the array structure to reduce complexity and redundancy in the web layer
		TreeMap<String, SourceDestinationAirports> airports = new TreeMap<String, SourceDestinationAirports>();
		AirportDataUtils.updateAirportMap(bookingTimeSeries, airports);
		
		return airports;
    }

	public IBookingTimeSeriesDAO getBookingTimeSeriesDAO() {
		return bookingTimeSeriesDAO;
	}

	public void setBookingTimeSeriesDAO(IBookingTimeSeriesDAO bookingTimeSeriesDAO) {
		this.bookingTimeSeriesDAO = bookingTimeSeriesDAO;
	}
	
}