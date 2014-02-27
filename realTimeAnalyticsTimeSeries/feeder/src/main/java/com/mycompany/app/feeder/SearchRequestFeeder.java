package com.mycompany.app.feeder;

import com.mycompany.app.common.dao.ISearchRequestDAO;
import com.mycompany.app.common.domain.SearchRequest;
import com.mycompany.app.common.utils.AirportDataUtils;
import com.mycompany.app.common.vo.SourceDestinationAirports;

import org.openspaces.core.SpaceInterruptedException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A feeder bean starts a scheduled task that writes a new Data objects to the space (in an unprocessed state).
 *
 * <p>The scheduling uses the java.util.concurrent Scheduled Executor Service. It
 * is started and stopped based on Spring life cycle events.
 */
public class SearchRequestFeeder extends Feeder implements InitializingBean, DisposableBean {

    private FeederTask feederTask;
    private ISearchRequestDAO searchRequestDAO;

    public void afterPropertiesSet() throws Exception {
    	log.info("--- STARTING SEARCH REQUEST FEEDER WITH CYCLE [" + defaultDelay + "]");
    	Assert.notNull(searchRequestDAO, "****** searchRequestDAO is a required property ******");
    	
    	executorService = Executors.newScheduledThreadPool(1);
        feederTask = new FeederTask();
        sf = executorService.scheduleAtFixedRate(feederTask, defaultDelay, defaultDelay, TimeUnit.MILLISECONDS);
    }

    public void destroy() throws Exception {
        sf.cancel(false);
        sf = null;
        executorService.shutdown();
    }
    
    public class FeederTask implements Runnable {
        private long counter = 1;

        public void run() {
            try {
                long time = System.currentTimeMillis();
                SearchRequest searchRequest = new SearchRequest("FEEDER " + Long.toString(time));
                
                SourceDestinationAirports airports = AirportDataUtils.generateRandomSourceDestinationAirPorts();
                searchRequest.setSourceDestinationId(airports.getId());
                searchRequest.setDestinationAirport(airports.getDesintationAirport());
                searchRequest.setSourceAirport(airports.getSourceAirport());
                searchRequest.setAirline(AirportDataUtils.generateRandomAirport());
                
                searchRequestDAO.save(searchRequest);
                //log.info("--- FEEDER WROTE SEARCH REQUEST " + searchRequest);
            }catch(SpaceInterruptedException e) {
                // ignore, we are being shutdown
            }catch(Exception e) {
                e.printStackTrace();
            }
            counter++;
        }

        public long getCounter() {
            return counter;
        }
    }
    
    public long getFeedCount() {
        return feederTask.getCounter();
    }

    public void setDefaultDelay(long defaultDelay) {
        this.defaultDelay = defaultDelay;
    }

	public void setSearchRequestDAO(ISearchRequestDAO searchRequestDAO) {
		this.searchRequestDAO = searchRequestDAO;
	}
   
}