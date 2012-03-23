package com.gigaspaces.server;

import java.util.concurrent.TimeUnit;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.core.transaction.manager.LocalJiniTxManagerConfigurer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Purpose of this class is to write domain objects.  Invoked from command line
 * 
 * @author dfilppi
 *
 */
public class DomainWriter {
	private int quantity;  //number to write
	private String location; //for domain object
	private int rate;  //write rate - accuracy limited to platform timer resolution
	private long duration;
	private GigaSpace gs=null;
	private String host;
	private String space;
	private PlatformTransactionManager tm;

	public int getQuantity() {
		return quantity;
	}

	@Option(name="-q",usage="number to write")
	public void setQuantity(int quantity){
		if(quantity<0)throw new RuntimeException("quantity cannot be negative");
		this.quantity = quantity;
	}

	public String getLocation() {
		return location;
	}

	@Option(name="-l",usage="location name",required=true)
	public void setLocation(String location) {
		this.location = location;
	}

	public int getRate() {
		return rate;
	}

	@Option(name="-r",usage="writes per second")
	public void setRate(int rate) {
		if(rate<0)throw new RuntimeException("rate cannot be negative");
		this.rate = rate;
	}

	public long getDuration() {
		return duration;
	}

	@Option(name="-d",usage="duration in ms")
	public void setDuration(long duration) {
		if(duration<0)throw new RuntimeException("duration cannot be negative");
		this.duration = duration;
	}

	@Option(name="-s",usage="space name",required=true)
	public void setSpace(String space) {
		this.space = space;
	}

	public String getSpace() {
		return space;
	}

	public void setGigaSpace(GigaSpace gs) {
		this.gs = gs;
	}

	public GigaSpace getGigaSpace() {
		return gs;
	}

	@Option(name="-h",usage="host name(locator)",required=true)
	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}
	public void run(){
		long sleepms=0;
		long sleepnano=0;
		long start=System.currentTimeMillis();
		int cnt=0;

		try{
			if(rate>0){
				sleepnano=TimeUnit.SECONDS.toNanos(1)/(long)(rate);
				if(sleepnano>1000000L){
					sleepms=sleepnano/1000000L;
					sleepnano%=1000000L;
				}
			}
			//quantity based
			if(quantity>0){
				for(int i=0;i<quantity;i++){
					DomainObject obj=new DomainObject(i,i,i,location);
					gs.write(obj);
					if(sleepms>0||sleepnano>0)Thread.sleep(sleepms,(int)sleepnano);
				}
			}
			//time based
			else if(duration>0){
				while(true){
					DomainObject obj=new DomainObject(cnt,cnt,cnt,location);
					TransactionStatus ts=tm.getTransaction(new DefaultTransactionDefinition());
					obj.setTime(System.currentTimeMillis());
					gs.write(obj);
					tm.commit(ts);
					cnt++;
					if(sleepms>0||sleepnano>0)Thread.sleep(sleepms,(int)sleepnano);
					if(System.currentTimeMillis()-start > duration)break;
				}
			}
		}
		catch(Exception e){
			if(e instanceof RuntimeException){
				throw (RuntimeException)e;
			}
			else{
				throw new RuntimeException(e);
			}
		}
		
		if(duration>0){
			System.out.println("objects written: "+cnt);
			System.out.println("actual write rate: "+((float)cnt)/(System.currentTimeMillis()-start)*1000);
		}
	}

	public static void main(String[] args)throws Exception{
		DomainWriter writer=new DomainWriter();
		new CmdLineParser(writer).parseArgument(args);

		if(writer.getQuantity()>0 && writer.getDuration()>0L){
			System.err.println("quantity and duration options are mutually exclusive");
			System.exit(1);
		}
		else if(writer.getQuantity()==0 && writer.getDuration()==0L){
			System.err.println("must specify either quantity or duration");
			System.exit(1);
		}

		UrlSpaceConfigurer sc=new UrlSpaceConfigurer("jini://"+writer.getHost()+"/*/"+writer.getSpace()+"?locators="+writer.getHost());
		//UrlSpaceConfigurer sc=new UrlSpaceConfigurer("jini://*/*/nyspace?groups=ONE");
		PlatformTransactionManager tm=new LocalJiniTxManagerConfigurer(sc.space()).transactionManager();
		GigaSpace gs=new GigaSpaceConfigurer(sc.space()).transactionManager(tm).gigaSpace();

		writer.setGigaSpace(gs);
		writer.setTm(tm);
		writer.run();
	}

	public void setTm(PlatformTransactionManager tm) {
		this.tm = tm;
	}

	public PlatformTransactionManager getTm() {
		return tm;
	}


}
