package org.openspaces.timeseries.service;

import java.util.List;

public interface TickGeneratorService {
	void setSymbols(List<List<String>> symbols);
	
	void clearTickSymbols();
	
	void startTicks();
	
	void stopTicks();
	

}
