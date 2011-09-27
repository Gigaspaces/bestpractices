package org.openspaces.timeseries.client;

import java.util.List;

import org.openspaces.timeseries.shared.Tick;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TicksServiceAsync {

	void setConfig(List<List<String>> settings,
			AsyncCallback<Void> callback);

	void start(AsyncCallback<Void> callback);

	void stop(AsyncCallback<Void> callback);

	void clear(AsyncCallback<Void> callback);

	void getTicks(String symbol, String[] types, Long start, Long end,
			AsyncCallback<Tick[]> asyncCallback);

	void getTotalTicks(AsyncCallback<Integer> callback);

	void clearSpace(AsyncCallback<Void> callback);

}
