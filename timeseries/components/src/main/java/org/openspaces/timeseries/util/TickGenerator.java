package org.openspaces.timeseries.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.timeseries.common.MarketDataEvent;

import com.gigaspaces.internal.backport.java.util.Arrays;

/**
 * Generates a phony tick stream to the space on command.
 * 
 * @author DeWayne
 *
 */
public class TickGenerator {
	static Logger log=Logger.getLogger(TickGenerator.class.getName());
	@GigaSpaceContext
	private GigaSpace space;
	private List<TickParms> symbols=new ArrayList<TickParms>();
	private long duration=600000;//10minutes
	private ExecutorService workerthread=Executors.newSingleThreadExecutor();
	private TickGeneratorWorker worker=new TickGeneratorWorker();
	private Future<Integer> future;
	private int startid=1;
	
	public TickGenerator(){
	}
	
	public synchronized void start(){
		log.info("start called");
		if(future!=null){
			log.info("stopping worker");
			try{
				//Wait for previous to complete.
				startid=future.get();
				startid++;
			}
			catch(Exception e){
				//throw new RuntimeException(e);
			}
		}
		if(symbols==null || symbols.size()==0)throw new RuntimeException(String.format("no symbols"));
		
		worker.setDuration(duration);
		worker.setStartid(startid);
		worker.setSymbols(symbols);
		worker.setSpace(space);
		

		log.info("started worker");
		future=workerthread.submit(worker);
	}
	
	public synchronized void addTickParms(TickParms tp){
		symbols.add(tp);
	}

	public TickParms[] getSymbols() {
		return symbols.toArray(new TickParms[]{});
	}

	@SuppressWarnings("unchecked")
	public void setSymbols(TickParms[] symbols) {
		this.symbols=new ArrayList<TickParms>();
		this.symbols.addAll(Arrays.asList(symbols));
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public int getStartid() {
		return startid;
	}

	public void setStartid(int startid) {
		this.startid = startid;
	}
	

	public GigaSpace getSpace() {
		return space;
	}

	public void setSpace(GigaSpace space) {
		this.space = space;
	}

	public void clearSymbols() {
		symbols.clear();
	}

	public synchronized void stop() {
		if(future==null || future.isDone())return;
		try{
			future.cancel(true);
		}
		catch(Exception e){
			log.severe("caught exception:"+e.getMessage());
		}
	}
}

class TickGeneratorWorker implements Callable<Integer>
{
	static Logger log=Logger.getLogger(TickGeneratorWorker.class.getName());
	private long duration;
	private long delayms=500;
	private int batchmin=10;
	private int batchmax=50;
	private GigaSpace space;
	private TickParms[] symbols;
	private int startid;
	private int lastid;
	private Random rand=new Random(System.currentTimeMillis());
	
	@Override
	public Integer call() {
		long start=System.currentTimeMillis();
		List<MarketDataEvent> ticks=new ArrayList<MarketDataEvent>();
		
		log.info(String.format("in call. duration=%d symbols count=%d",duration,symbols.length));
		
		lastid=startid;
		
		
		while(System.currentTimeMillis()-start < duration){
			//throttle
			try {
				Thread.sleep(delayms);
			} catch (InterruptedException e) {
				return lastid;
			}
			
			
			//Generate a block of ticks (10-max) and write them
			int size=rand.nextInt(batchmax-batchmin)+batchmin;
			ticks.clear();
			
			for(int i=0;i<size;i++){
				int index=rand.nextInt(symbols.length);
				
				TickParms symbol=symbols[index];
				long now=System.currentTimeMillis();
				
				//Gen 3/4 quotes/ticks
				boolean trade=(rand.nextInt(4)==0);
				MarketDataEvent tick=new MarketDataEvent(!trade);
				if(trade){
					double nextprice=(symbol.getLastask()+symbol.getLastbid())/2;
					if(nextprice==0)nextprice=.05;
					tick.getTradeData().setPrice(nextprice);
					//tick.getTradeData().setVolume(rand.nextInt(2)==0?symbol.getLastbidvol():symbol.getLastaskvol()); //"realistic" version
					tick.getTradeData().setVolume(rand.nextInt(symbol.getMaxvol()-symbol.getMinvol())+symbol.getMinvol());  //creates big swings for better graphs
					tick.setEventType("trade");
					
					symbol.setLastprice(nextprice);
				}
				else{ //quote
					
					if(symbol.getLastask()==0 || symbol.getLastbid()==0){
						symbol.setLastask(symbol.getPriceBasis()+.05);
						symbol.setLastbid(symbol.getPriceBasis()-.05);
					}
					//This is rather dumb. Not realistic but good enough.
					//bid or ask?
					if(rand.nextInt(2)==0){//new bid
						double nextbid=symbol.getLastbid()+(rand.nextDouble()*.20-.1);
						if(nextbid>=symbol.getLastask())nextbid=symbol.getLastask()-.05;
						if(nextbid<0)nextbid=.05;
						tick.getQuoteData().setBid(nextbid);
						tick.getQuoteData().setBidvol((rand.nextInt(symbol.getMaxvol()-symbol.getMinvol())+symbol.getMinvol()));
						tick.getQuoteData().setAsk(symbol.getLastask());
						tick.getQuoteData().setAskvol(symbol.getLastaskvol());
						symbol.setLastbidvol(tick.getQuoteData().getBidvol());
						symbol.setLastbid(nextbid);
					}
					else{  //new ask
						double nextask=symbol.getLastask()+(rand.nextDouble()*.2-.1);
						if(nextask<=symbol.getLastbid())nextask=symbol.getLastbid()+5;
						tick.getQuoteData().setAsk(nextask);
						tick.getQuoteData().setAskvol((rand.nextInt(symbol.getMaxvol()-symbol.getMinvol())+symbol.getMinvol()));
						tick.getQuoteData().setBid(symbol.getLastbid());
						tick.getQuoteData().setBidvol(symbol.getLastbidvol());
						symbol.setLastaskvol(tick.getQuoteData().getAskvol());
						symbol.setLastask(nextask);
					}
					tick.setEventType("quote");
				}
				tick.setSequence(lastid++);
				tick.setSymbol(symbol.getSymbol());
				tick.setEventTime(now-(now%1000)); //trunc to second
				ticks.add(tick);
			}
			
			//Write the block into the space
			if(ticks.size()>0)space.writeMultiple(ticks.toArray());
		}
		
		return lastid;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public GigaSpace getSpace() {
		return space;
	}

	public void setSpace(GigaSpace space) {
		this.space = space;
	}

	public void setSymbols(List<TickParms> symbols) {
		log.info("setting symbols, cnt="+symbols.size());
		this.symbols = symbols.toArray(new TickParms[]{});
	}

	public int getStartid() {
		return startid;
	}

	public void setStartid(int startid) {
		this.startid = startid;
	}


}

