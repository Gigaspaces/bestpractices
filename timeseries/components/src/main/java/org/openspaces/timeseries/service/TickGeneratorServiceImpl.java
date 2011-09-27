package org.openspaces.timeseries.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openspaces.remoting.RemotingService;
import org.openspaces.timeseries.processor.PeriodTimer;
import org.openspaces.timeseries.util.TickGenerator;
import org.openspaces.timeseries.util.TickParms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@RemotingService(exporter="serviceExporter")
public class TickGeneratorServiceImpl implements TickGeneratorService {
	static Logger log=Logger.getLogger(TickGeneratorServiceImpl.class.getName());

	@Autowired
	ApplicationContext ctx;

	@Override
	public void clearTickSymbols() {
		ctx.getBean(TickGenerator.class).clearSymbols();
	}

	@Override
	public void startTicks() {
		ctx.getBean(PeriodTimer.class).start();
		ctx.getBean(TickGenerator.class).start();
	}

	@Override
	public void stopTicks() {
		ctx.getBean(PeriodTimer.class).stop();
		ctx.getBean(TickGenerator.class).stop();
	}

	@Override
	public void setSymbols(List<List<String>> symbols) {
		log.info("set symbols called");

		clearTickSymbols();
		
		final List<TickParms> tp=new ArrayList<TickParms>();

		try{

			TickParms p;
			for(List<String> m:symbols){
				p=new TickParms();
				p.setSymbol(m.get(0));
				p.setMinvol(Integer.parseInt(m.get(1)));
				p.setMaxvol(Integer.parseInt(m.get(2)));
				p.setPriceBasis(Double.parseDouble(m.get(3)));
				tp.add(p);
			}

		}catch(Exception e){
			log.severe("caught exception parsing parms: "+e.getMessage());
		}

		log.info(String.format("calling setSymbols with %d symbols\n",tp.size()));
		try{
			ctx.getBean(TickGenerator.class).setSymbols(tp.toArray(new TickParms[]{}));
		}
		catch(Exception e){
			log.severe("caught exception setting parms: "+e.getMessage());
		}
	}
	

}
