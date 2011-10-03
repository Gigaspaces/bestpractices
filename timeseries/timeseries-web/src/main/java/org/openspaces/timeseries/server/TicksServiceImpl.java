package org.openspaces.timeseries.server;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openspaces.timeseries.client.TicksService;
import org.openspaces.timeseries.service.MarketDataService;
import org.openspaces.timeseries.service.TickGeneratorService;
import org.openspaces.timeseries.shared.Tick;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class TicksServiceImpl extends RemoteServiceServlet implements
		TicksService{
	private static final long serialVersionUID = 1L;
	static Logger log=Logger.getLogger(TicksServiceImpl.class.getName());
	
	TickGeneratorService tickproxy;
	MarketDataService mdsproxy;
	
	@Override
	public void init(){
		tickproxy=(TickGeneratorService)getServletContext().getAttribute("ticksProxy");
		mdsproxy=(MarketDataService)getServletContext().getAttribute("mdsProxy");
	}


	public void start() {
		tickproxy.startTicks();
	}

	public void stop() {
		tickproxy.stopTicks();
	}

	public void clear() {
		tickproxy.clearTickSymbols();
	}


	public void setConfig(List<List<String>> settings) {
		tickproxy.setSymbols(settings);
	}

	@Override
	public Tick[] getTicks(String symbol, String[] types, Long start, Long end) {
		String[][] res=mdsproxy.getTicks(symbol, types, start, end);
		List<Tick> outlist=new ArrayList<Tick>();
		if(res==null)return new Tick[]{};
		log.info(String.format("gwt server: called"));
		for(String[] l:res){
			Tick t =new Tick();
			t.setSymbol(l[0]);
			t.setType(l[1]);
			t.setTime(Long.valueOf(l[2]));
			List<String> sl=new ArrayList<String>();
			for(int i=3;i<l.length;i++)sl.add(l[i]);
			t.setValue(sl);
			log.info(String.format("gwt server: got tick %s",t));
			outlist.add(t);
		}
		return outlist.toArray(new Tick[]{}); 
	}


	@Override
	public int getTotalTicks() {
		return mdsproxy.getTotalTicks();
	}


	@Override
	public void clearSpace() {
		mdsproxy.clear();
	}


}

