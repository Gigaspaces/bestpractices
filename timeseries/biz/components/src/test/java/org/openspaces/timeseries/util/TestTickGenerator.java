package org.openspaces.timeseries.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.timeseries.common.MarketDataEvent;
import org.openspaces.timeseries.util.TickGenerator.TickParms;

import com.j_spaces.core.client.ReadModifiers;

public class TestTickGenerator {
	private static GigaSpace space;
	private static UrlSpaceConfigurer cfg;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		cfg=new UrlSpaceConfigurer("/./myspace");
		cfg.fifo(true);
		space=new GigaSpaceConfigurer(cfg.space()).gigaSpace();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if(cfg!=null)cfg.destroy();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws InterruptedException {
		
		TickGenerator gen=new TickGenerator();
		
		gen.setSpace(space);
		TickParms tp=new TickParms("X",1,1000,10000);
		TickParms tp2=new TickParms("Y",1,1000,12000);
		gen.setSymbols(new TickParms[]{tp,tp2});
		gen.start();
		Thread.sleep(2000);
		
		MarketDataEvent[] ticks=space.readMultiple(new MarketDataEvent(),1000,ReadModifiers.FIFO);
		//Do some basic checks
		for(MarketDataEvent e:ticks){
			if(e.getSymbol().equals("X") || e.getSymbol().equals("Y")){
				if(e.getQuoteData()!=null){
					if(e.getQuoteData().getBid()<=0 || e.getQuoteData().getBid()>=e.getQuoteData().getAsk()){
						fail("invalid bid:"+e.getQuoteData().getBid());
					}
					if(e.getQuoteData().getAsk()<=0 || e.getQuoteData().getAsk()<=e.getQuoteData().getBid()){
						fail("invalid ask:"+e.getQuoteData().getAsk());
					}
					if(e.getQuoteData().getBidvol()<=0)fail("invalid bid volume:"+e.getQuoteData().getBidvol());
					if(e.getQuoteData().getAskvol()<=0)fail("invalid ask volume:"+e.getQuoteData().getAskvol());
					if(!e.getEventType().equals("quote"))fail("wrong event type:"+e.getEventType());
				}
				else{
					if(e.getTradeData().getPrice()<=0)fail("invalid price:"+e.getTradeData().getPrice());
					if(e.getTradeData().getVolume()<=0)fail("invalid volume:"+e.getTradeData().getVolume());
					if(!e.getEventType().equals("trade"))fail("wrong event type:"+e.getEventType());
				}
			}
			else{
				fail("unknown tick symbol:"+e.getSymbol());
			}
		}
	}

}
