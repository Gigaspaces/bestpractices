package com.gigaspaces.server;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.core.transaction.manager.LocalJiniTxManagerConfigurer;
import org.springframework.transaction.PlatformTransactionManager;

import com.gigaspaces.server.ReplicationStatus.Status;


public class TestReplicationMonitor {
	private static UrlSpaceConfigurer sc=null;
	private static GigaSpace gs=null;
	private static PlatformTransactionManager tm=null;

	@BeforeClass
	public static void beforeClass() throws Exception{
		sc=new UrlSpaceConfigurer("/./space");
		tm=new LocalJiniTxManagerConfigurer(sc).transactionManager();
		gs=new GigaSpaceConfigurer(sc.space()).transactionManager(tm).gigaSpace();
	}
	
	public static void before(){
		gs.clear(new Object());
	}
	
	@Test
	public void testDegraded() throws Exception{
		TimeRecord nyt=new TimeRecord("ny");
		long now=System.currentTimeMillis();
		for(long i=0;i<10L;i++){
			TimeStamp nyts=new TimeStamp("ny");
			nyts.setTime(now-6000L);
			nyt.setTime(nyts);
		}

		TimeRecord njt=new TimeRecord("nj");
		for(long i=0;i<10L;i++){
			TimeStamp njts=new TimeStamp("nj");
			njts.setTime(now-1000L);
			njt.setTime(njts);
		}

		gs.write(nyt);
		gs.write(njt);

		ReplicationMonitor mon=new ReplicationMonitor();
		mon.setSpace(gs);
		mon.setDegradedLatencyThreshold(2000L);
		mon.setDownLatencyThreshold(10000L);
		mon.setTimestampRate(1000);
		mon.init();
		Thread.sleep(2000);
		verifyStatus(Status.DEGRADED);
		mon.stop();
	}
	
	@Test
	public void testDown() throws Exception{
		TimeRecord nyt=new TimeRecord("ny");
		long now=System.currentTimeMillis();
		for(long i=0;i<10L;i++){
			TimeStamp nyts=new TimeStamp("ny");
			nyts.setTime(now-16000L);
			nyt.setTime(nyts);
		}

		TimeRecord njt=new TimeRecord("nj");
		for(long i=0;i<10L;i++){
			TimeStamp njts=new TimeStamp("nj");
			njts.setTime(now-1000L);
			njt.setTime(njts);
		}

		gs.write(nyt);
		gs.write(njt);

		ReplicationMonitor mon=new ReplicationMonitor();
		mon.setSpace(gs);
		mon.setDegradedLatencyThreshold(2000L);
		mon.setDownLatencyThreshold(10000L);
		mon.setTimestampRate(1000);
		mon.init();
		Thread.sleep(2000);
		verifyStatus(Status.DOWN);
		mon.stop();
	}
	
	/*
	 * The works.  Test start, normal, degraded, down, and back to normal
	 */
	@Test
	public void testAll() throws Exception{
		ReplicationMonitor mon=new ReplicationMonitor();
		mon.setSpace(gs);
		mon.setDegradedLatencyThreshold(3000L);
		mon.setDownLatencyThreshold(10000L);
		mon.setTimestampRate(1000);
		mon.init();
		Thread.sleep(1500);
		verifyStatus(Status.DOWN);
		
		TimeRecord nyt=new TimeRecord("ny");
		long now=System.currentTimeMillis();
		for(long i=0;i<10L;i++){
			TimeStamp nyts=new TimeStamp("ny");
			nyts.setTime(now-1000L-(10L-i)*1000L);//should cause degraded
			nyt.setTime(nyts);
		}
		gs.write(nyt);
		Thread.sleep(1500);
		verifyStatus(Status.DEGRADED);
		
		now=System.currentTimeMillis();
		for(long i=0;i<5L;i++){
			TimeStamp nyts=new TimeStamp("ny");
			nyts.setTime(now-1000L-(10L-i)*50L);//should cause up
			nyt.setTime(nyts);
		}
		gs.write(nyt);
		Thread.sleep(1500);
		verifyStatus(Status.UP);
		
		//Degrade
		now=System.currentTimeMillis();
		for(long i=0;i<5L;i++){
			TimeStamp nyts=new TimeStamp("ny");
			nyts.setTime(now-1000L-(10L-i)*1000L);//should cause up
			nyt.setTime(nyts);
		}
		gs.write(nyt);
		Thread.sleep(1500);
		verifyStatus(Status.DEGRADED);
	}
	
	private void verifyStatus(ReplicationStatus.Status status)throws Exception{
		ReplicationStatus st=gs.read(new ReplicationStatus());
		if(st==null)Assert.fail("replication status missing");
		Assert.assertTrue(status==st.getStatus());
	}
}
