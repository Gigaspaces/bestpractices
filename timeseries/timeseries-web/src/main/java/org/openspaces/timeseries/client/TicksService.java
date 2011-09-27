package org.openspaces.timeseries.client;

import java.util.List;

import org.openspaces.timeseries.shared.Tick;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath(value="ticksService")
public interface TicksService extends RemoteService {
	void setConfig(List<List<String>> settings);
	void clear();
	void start();
	void stop();
	Tick[] getTicks(String symbol,String[] types,Long start,Long end);
	int getTotalTicks();
	void clearSpace();
}
