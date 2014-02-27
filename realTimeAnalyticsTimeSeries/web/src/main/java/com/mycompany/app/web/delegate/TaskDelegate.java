package com.mycompany.app.web.delegate;

import java.util.TreeMap;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;

import com.gigaspaces.async.AsyncFuture;
import com.mycompany.app.common.vo.SourceDestinationAirports;
import com.mycompany.app.web.tasks.DetermineInitialIntervalTask;
import com.mycompany.app.web.tasks.RetrieveNextActiveBookingTimeSeriesTask;
import com.mycompany.app.web.tasks.distributed.RetrieveCompletedBookingTimeSeriesTask;


public class TaskDelegate {
	
	@GigaSpaceContext(name = "gigaSpace")
	private GigaSpace gigaSpace;
	
	public TaskDelegate() {}
	
	public void setGigaSpace(GigaSpace gigaSpace) {
		this.gigaSpace = gigaSpace;
	}
	
	public TreeMap<String, SourceDestinationAirports> retrieveCompletedBookingTimeSeriesIntervals(String airline, Integer maxIntervals) {
		AsyncFuture<Integer> determineInitialIntervalResult = gigaSpace.execute(new DetermineInitialIntervalTask(airline, maxIntervals));
		
		Integer lastInterval = null;
		try {
			lastInterval = determineInitialIntervalResult.get();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		AsyncFuture<TreeMap<String, SourceDestinationAirports>> retrieveCompletedBookingTimeSeriesResult
												= gigaSpace.execute(new RetrieveCompletedBookingTimeSeriesTask(airline, lastInterval));
		
		TreeMap<String, SourceDestinationAirports> airports = null;
		try {
			airports = retrieveCompletedBookingTimeSeriesResult.get();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return airports;
	}
	
	public TreeMap<String, SourceDestinationAirports> retrieveNextActiveBookingTimeSeries(String airline, Integer lastInterval) {
		AsyncFuture<TreeMap<String, SourceDestinationAirports>> retrieveNextActiveBookingTimeSeriesResult
												= gigaSpace.execute(new RetrieveNextActiveBookingTimeSeriesTask(airline, lastInterval));
		
		TreeMap<String, SourceDestinationAirports> airports = null;
		try {
			airports = retrieveNextActiveBookingTimeSeriesResult.get();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return airports;
	}
	
}
