package com.gigaspaces.server;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestTimeRecord {
	@Test
	public void testSet(){
		TimeRecord record=new TimeRecord("test");
		TimeStamp ts=new TimeStamp();
		ts.setTime(3L);
		record.setTime(ts);
		assertEquals(record.getTime(),new Long(3));
	}

	@Test
	public void testSet4(){
		TimeRecord record=new TimeRecord("test");
		for(int i=0;i<4;i++){
			TimeStamp ts=new TimeStamp();
			ts.setTime((long)i);
			record.setTime(ts);
		}
		assertEquals(record.getTime(),new Long(3));
	}
	
	@Test
	public void testSimpleLatency() throws InterruptedException{
		TimeRecord record=new TimeRecord("test");
		TimeStamp ts=new TimeStamp("test");
		Thread.sleep(1000L);
		record.setTime(ts);
		assertTrue(record.getAveLatency()>999.0 && record.getAveLatency()<1011.0);
	}

	@Test
	public void testFullLatency() throws InterruptedException{
		TimeRecord record=new TimeRecord("test");
		for(int i=0;i<4;i++){
			TimeStamp ts=new TimeStamp("test");
			Thread.sleep(400);
			record.setTime(ts);
		}
		assert(record.getAveLatency()>380 && record.getAveLatency()<440);
	}
}
