package org.openspaces.timeseries.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.timeseries.common.MarketDataEvent;
import org.openspaces.timeseries.util.TickGenerator.TickParms;

import com.gigaspaces.internal.backport.java.util.Arrays;

/**
 * Generates a phony tick stream to the space on command.
 * 
 * @author DeWayne
 *
 */
public class TickGenerator {
	@GigaSpaceContext
	private GigaSpace space;
	private List<TickParms> symbols=new ArrayList<TickParms>();
	private long duration=1000;
	private ExecutorService workerthread=Executors.newSingleThreadExecutor();
	private TickGeneratorWorker worker=new TickGeneratorWorker();
	private Future<Integer> future;
	private int startid=1;
	
	public TickGenerator(){
	}
	
	public synchronized void start(){
		if(future!=null){
			try{
				//Wait for previous to complete.
				startid=future.get();
				startid++;
			}
			catch(Exception e){
				throw new RuntimeException(e);
			}
		}
		if(symbols==null )throw new RuntimeException("no symbols");
		
		worker.setDuration(duration);
		worker.setStartid(startid);
		worker.setSymbols(symbols);
		worker.setSpace(space);

		future=workerthread.submit(worker);
	}
	
	public void addTickParms(TickParms tp){
		symbols.add(tp);
	}

	public TickParms[] getSymbols() {
		return symbols.toArray(new TickParms[]{});
	}

	@SuppressWarnings("unchecked")
	public void setSymbols(TickParms[] symbols) {
		this.symbols =(List<TickParms>)Arrays.asList(symbols);
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
	
	public static class TickParms{
		private String symbol;
		private int minvol,maxvol;
		private double priceBasis;
		private double lastprice;
		private double lastbid;
		private double lastask;
		private int lastbidvol;
		private int lastaskvol;
		
		public TickParms(){}
		
		public TickParms(String symbol, int minvol, int maxvol,
				int priceBasis) {
			super();
			this.symbol = symbol;
			this.minvol = minvol;
			this.maxvol = maxvol;
			this.priceBasis = priceBasis;
			this.lastprice=priceBasis;
			this.lastbid=this.lastprice-5;
			this.lastask=this.lastprice+5;
			lastbidvol=(minvol+maxvol)/2;
			lastaskvol=lastbidvol;  //dumb defaults
		}
		
		public String getSymbol() {
			return symbol;
		}
		public void setSymbol(String symbol) {
			this.symbol = symbol;
		}
		public int getMinvol() {
			return minvol;
		}
		public void setMinvol(int minvol) {
			this.minvol = minvol;
		}
		public int getMaxvol() {
			return maxvol;
		}
		public void setMaxvol(int maxvol) {
			this.maxvol = maxvol;
		}
		public double getPriceBasis() {
			return priceBasis;
		}
		public void setPriceBasis(double priceBasis) {
			this.priceBasis = priceBasis;
			this.lastprice=priceBasis;
			this.lastbid=this.lastprice-.05;
			this.lastask=this.lastprice+.05;
		}

		public double getLastprice() {
			return lastprice;
		}

		public void setLastprice(double lastprice) {
			this.lastprice = lastprice;
		}

		public double getLastbid() {
			return lastbid;
		}

		public void setLastbid(double lastbid) {
			this.lastbid = lastbid;
		}

		public double getLastask() {
			return lastask;
		}

		public void setLastask(double lastask) {
			this.lastask = lastask;
		}

		public int getLastbidvol() {
			return lastbidvol;
		}

		public void setLastbidvol(int lastbidvol) {
			this.lastbidvol = lastbidvol;
		}

		public int getLastaskvol() {
			return lastaskvol;
		}

		public void setLastaskvol(int lastaskvol) {
			this.lastaskvol = lastaskvol;
		}

	}

	public GigaSpace getSpace() {
		return space;
	}

	public void setSpace(GigaSpace space) {
		this.space = space;
	}
}

class TickGeneratorWorker implements Callable<Integer>
{
	private long duration;
	private GigaSpace space;
	private TickParms[] symbols;
	private int startid;
	private int lastid;
	private Random rand=new Random(System.currentTimeMillis());
	
	@Override
	public Integer call() {
		long start=System.currentTimeMillis();
		List<MarketDataEvent> ticks=new ArrayList<MarketDataEvent>();
		
		lastid=startid;
		
		while(System.currentTimeMillis()-start < duration){
			//throttle
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			
			//Generate a block of ticks and write them
			int size=rand.nextInt(100)+10;
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
					tick.getTradeData().setVolume(rand.nextInt(2)==0?symbol.getLastbidvol():symbol.getLastaskvol());
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
			space.writeMultiple(ticks.toArray());
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
		this.symbols = symbols.toArray(new TickParms[0]);
	}

	public int getStartid() {
		return startid;
	}

	public void setStartid(int startid) {
		this.startid = startid;
	}


}

