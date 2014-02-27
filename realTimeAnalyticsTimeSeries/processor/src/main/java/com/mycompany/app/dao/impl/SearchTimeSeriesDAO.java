package com.mycompany.app.dao.impl;

import java.util.Date;

import com.gigaspaces.client.ChangeSet;
import com.gigaspaces.query.IdQuery;
import com.j_spaces.core.client.SQLQuery;
import com.mycompany.app.common.dao.ISearchTimeSeriesDAO;
import com.mycompany.app.common.domain.SearchTimeSeries;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.springframework.stereotype.Component;

@Component
public class SearchTimeSeriesDAO implements ISearchTimeSeriesDAO {

	@GigaSpaceContext(name = "gigaSpace")
	private GigaSpace gigaSpace;
	
	public void setGigaSpace(GigaSpace gigaSpace) {
		this.gigaSpace = gigaSpace;
	}
	
	public SearchTimeSeries[] readAllSearchTimeSeries() {
		return gigaSpace.readMultiple(new SearchTimeSeries());
	}
	
	//SearchTimeSeries will only remain in the space for 24 seconds which correlates to 50+ intervals @ 2 seconds each
	public void save(SearchTimeSeries searchTimeSeries) {
		gigaSpace.write(searchTimeSeries, 24000);
	}
	
	public SearchTimeSeries findSearchTimeSeriesWithinCurrentInterval(String airline, long timeSeriesInterval) {
		SQLQuery<SearchTimeSeries> query = new SQLQuery<SearchTimeSeries>(SearchTimeSeries.class, "airline = ? and lasttimestamp > ?");
        query.setParameter(1, airline);
        query.setParameter(2, new Date(System.currentTimeMillis() - timeSeriesInterval));
        
        return gigaSpace.read(query);
	}
	
	public void incrementSourceDestinationCounter(SearchTimeSeries searchTimeSeries, String sourceDestination) {
		IdQuery<SearchTimeSeries> idQuery = new IdQuery<SearchTimeSeries>(SearchTimeSeries.class, searchTimeSeries.getIntervalId());
		gigaSpace.change(idQuery, new ChangeSet().increment("sourceDestinationCounter." + sourceDestination, 1));
	}
	
}