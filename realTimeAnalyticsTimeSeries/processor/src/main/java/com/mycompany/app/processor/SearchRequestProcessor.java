package com.mycompany.app.processor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;
import org.springframework.util.Assert;

import com.mycompany.app.common.dao.ISearchTimeSeriesDAO;
import com.mycompany.app.common.domain.SearchRequest;
import com.mycompany.app.common.domain.SearchTimeSeries;
import com.mycompany.app.common.utils.AirportDataUtils;

/**
 * The processor simulates work done on unprocessed SearchRequest objects. The processData accepts a SearchRequest object,
 * simulates work by sleeping, and then sets the processed flag to true and returns the processed SearchRequest.
 */
@Polling(concurrentConsumers=10)
public class SearchRequestProcessor {
	
    private Logger log = Logger.getLogger(this.getClass().getName());

    private long timeSeriesInterval;
    private Map<String, Integer> intervalAirlineMap = new HashMap<String, Integer>(4);
    
    private ISearchTimeSeriesDAO searchTimeSeriesDAO;
   

    @SpaceDataEvent
    public void processData(SearchRequest searchRequest, GigaSpace space) {
    	Assert.notNull(searchTimeSeriesDAO, "**** searchTimeSeriesDAO is a required property ****");
    	
    	searchRequest.setProcessedData("PROCESSED : " + searchRequest.getRawData());
    	searchRequest.setProcessed(true);
    	String sourceDestinationAirport = AirportDataUtils.generateSourceDestinationKey(searchRequest.getSourceAirport(), searchRequest.getDestinationAirport());
    	//log.info(" ------ SEARCH REQUEST PROCESSED : " + searchRequest);
                
        SearchTimeSeries result = searchTimeSeriesDAO.findSearchTimeSeriesWithinCurrentInterval(searchRequest.getAirline(), timeSeriesInterval);        
        if(result != null)
    		searchTimeSeriesDAO.incrementSourceDestinationCounter(result, sourceDestinationAirport);
        else
    		createNewInterval(searchRequest.getAirline(), sourceDestinationAirport);
    }
    
    private void createNewInterval(String airline, String sourceDestination) {
    	Integer interval  = intervalAirlineMap.get(airline);
		if(interval != null)
			intervalAirlineMap.put(airline, ++interval);
		else {
			interval = 1;
			intervalAirlineMap.put(airline, interval);
		}
		
    	Map<String, Integer> newSourceDestinationCounterMap = new HashMap<String, Integer>();
    	newSourceDestinationCounterMap.put(sourceDestination, 1);
    	
		SearchTimeSeries searchTimeSeries = new SearchTimeSeries(interval, airline);
    	searchTimeSeries.setSourceDestinationCounter(newSourceDestinationCounterMap);
    	searchTimeSeries.setLasttimestamp(new Date(System.currentTimeMillis()));
    	searchTimeSeriesDAO.save(searchTimeSeries);
    }
    
    public void setTimeSeriesInterval(long timeSeriesInterval) {
		this.timeSeriesInterval = timeSeriesInterval;
	}

	public void setSearchTimeSeriesDAO(ISearchTimeSeriesDAO searchTimeSeriesDAO) {
		this.searchTimeSeriesDAO = searchTimeSeriesDAO;
	}

}