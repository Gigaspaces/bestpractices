package com.mycompany.app.common.dao;

import com.mycompany.app.common.domain.SearchTimeSeries;

public interface ISearchTimeSeriesDAO {
	
	SearchTimeSeries[] readAllSearchTimeSeries();
	
	void save(SearchTimeSeries searchTimeSeries);

	SearchTimeSeries findSearchTimeSeriesWithinCurrentInterval(String airline, long timeSeriesInterval);
	
	void incrementSourceDestinationCounter(SearchTimeSeries searchTimeSeries, String sourceDestination);
	
}