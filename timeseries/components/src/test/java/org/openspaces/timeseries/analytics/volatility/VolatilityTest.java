package org.openspaces.timeseries.analytics.volatility;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;
import org.openspaces.timeseries.analytics.average.AverageEventData;
import org.openspaces.timeseries.analytics.vwap.VwapEventData;
import org.openspaces.timeseries.common.MarketDataEvent;
import org.openspaces.timeseries.processor.PeriodTimer;
import org.openspaces.timeseries.util.TickGenerator;
import org.springframework.context.ApplicationContext;

public class VolatilityTest {
	
	static IntegratedProcessingUnitContainer container=null;
	
	@BeforeClass
	public static void beforeClass() throws IOException{
		
		IntegratedProcessingUnitContainerProvider provider=new IntegratedProcessingUnitContainerProvider();		

		ClusterInfo clusterInfo = new ClusterInfo();
		clusterInfo.setSchema("partitioned-sync2backup");
		clusterInfo.setNumberOfInstances(1);
		clusterInfo.setNumberOfBackups(0);
		clusterInfo.setInstanceId(1);
		provider.setClusterInfo(clusterInfo);

		// set the config location (override the default one - classpath:/META-INF/spring/pu.xml)
		provider.addConfigLocation("classpath:test-pu.xml");

		// Build the Spring application context and "start" it
		container = (IntegratedProcessingUnitContainer)provider.createContainer();
	}
	
	@AfterClass
	public static void afterClass(){
		try {
			if(container!=null)container.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testVolatility() throws InterruptedException{
		ApplicationContext ac=container.getApplicationContext();
		TickGenerator t=ac.getBean(TickGenerator.class);
		PeriodTimer pt=ac.getBean(PeriodTimer.class);
		GigaSpace space=ac.getBean(GigaSpace.class);
		
		pt.start();
		Thread.sleep(300);
		t.setDuration(8000);
		t.start();
		Thread.sleep(5000);
		
		MarketDataEvent template=new MarketDataEvent();
		template.setEventType("volatility");
		MarketDataEvent[] events=space.readMultiple(template);
		if(events==null )fail("no volatilities generated");
		for(MarketDataEvent e:events){
			System.out.println("vol="+((VolatilityEventData)e.getSynthData()).getVolatility());
		}
		
		template.setEventType("vwap");
		events=space.readMultiple(template);
		if(events==null )fail("no vwaps generated");
		for(MarketDataEvent e:events){
			System.out.println("vwap="+((VwapEventData)e.getSynthData()).getVwap());
		}
		
		template.setEventType("average");
		events=space.readMultiple(template);
		if(events==null )fail("no averages generated");
		for(MarketDataEvent e:events){
			System.out.println("ave="+((AverageEventData)e.getSynthData()).getAverage());
		}
	}
}
