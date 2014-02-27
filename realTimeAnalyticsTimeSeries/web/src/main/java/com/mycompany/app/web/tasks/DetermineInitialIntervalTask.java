package com.mycompany.app.web.tasks;

import javax.annotation.Resource;

import org.openspaces.core.executor.AutowireTask;
import org.openspaces.core.executor.Task;

import com.gigaspaces.annotation.pojo.SpaceRouting;
import com.mycompany.app.common.dao.IBookingTimeSeriesDAO;
import com.mycompany.app.common.domain.BookingTimeSeries;

@AutowireTask
public class DetermineInitialIntervalTask implements Task<Integer> {

	private static final long serialVersionUID = -2846168106076276015L;
	
	private String airline;
	private Integer maxIntervals;
    
	@Resource
	private transient IBookingTimeSeriesDAO bookingTimeSeriesDAO;
	
	public DetermineInitialIntervalTask() {};
	
    public DetermineInitialIntervalTask(String airline, Integer maxIntervals) {
		this.airline = airline;
		this.maxIntervals = maxIntervals;
	}
    
    @SpaceRouting
    public String getAirline() {
		return airline;
    }
    
    public Integer execute() throws Exception {    	
    	BookingTimeSeries activeBookingTimeSeries = bookingTimeSeriesDAO.readActiveTimeSeriesByAirline(airline);
		
		//If applicable, limits the initial graph rendering to a max of provided intervals
		Integer activeInterval = activeBookingTimeSeries.getInterval();
		if(activeInterval > maxIntervals) {
			return activeInterval - maxIntervals;
		}
		return 0;
    }

	public IBookingTimeSeriesDAO getBookingTimeSeriesDAO() {
		return bookingTimeSeriesDAO;
	}

	public void setBookingTimeSeriesDAO(IBookingTimeSeriesDAO bookingTimeSeriesDAO) {
		this.bookingTimeSeriesDAO = bookingTimeSeriesDAO;
	}
	
}