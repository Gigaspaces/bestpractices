package org.openspaces.timeseries.processor;

import java.util.Date;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentSkipListSet;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;

/**
 * Creates timer events and adds them to the space on a regular interval (max
 * resolution/min interval 1 second).  Events must be a multiple, or an evenly
 * divisible fraction of a minute (e.g. "7" isn't a valid period). Events are ephemeral,
 * in the sense that their lease is short, just long enough to get handled.
 * 
 * @author DeWayne
 *
 */
public class PeriodTimer extends TimerTask {
	private Timer timer=new Timer();
	private long startMinute;
	private boolean active=false;
	private Set<Integer> periods=new ConcurrentSkipListSet<Integer>();
	
	@GigaSpaceContext(name="gigaSpace")
	private GigaSpace space;
	
	public PeriodTimer(){
		long now=System.currentTimeMillis();
		timer.scheduleAtFixedRate(this, new Date(now+(1000L-now%1000L)), 1000);
	}
	
	public void start(){
		active=true;
		long now=System.currentTimeMillis();
		startMinute=now-(now%60000);
	}
	public void stop(){
		active=false;
	}

	@Override
	public void run() {
		if(active){
			long now=System.currentTimeMillis();
			now=now-(now%1000L);  //ensure second resolution
			for(int period:periods){
				if(((now-startMinute)%period)==0){
					PeriodTimerEvent ped=new PeriodTimerEvent(now,period/1000);
					space.write(ped,10000L);
				}
			}
		}
	}
	
	public void addPeriod(int periodSecs)
	{
		if(periodSecs<=0)throw new RuntimeException("invalid period:"+periodSecs);
		periods.add(periodSecs*1000);
	}
	
	public static void main(String[] args) throws Exception{
		PeriodTimer t=new PeriodTimer();
		t.addPeriod(5);
		t.active=true;
		Thread.sleep(10000);
	}
	
	
}
