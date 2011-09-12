package org.openspaces.timeseries.util;

import java.util.Calendar;

public class DateUtil {

	public static long getFirstMillisecond(long currentTime, String periodType) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(currentTime);
		
		if (periodType.equals("minute")) {
	        c.set(Calendar.SECOND, 0);
	        c.set(Calendar.MILLISECOND, 0);
		} else if (periodType.equals("day")) {
			c.set(Calendar.SECOND, 0);
	        c.set(Calendar.MILLISECOND, 0);
	        c.set(Calendar.HOUR_OF_DAY, 0);
		}
		return c.getTimeInMillis();

	}
	
	public static long getLastMillisecond(long currentTime, String periodType) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(currentTime);
		
		if (periodType.equals("minute")) {
			c.add(Calendar.MINUTE, 1);
	        c.set(Calendar.SECOND, 0);
	        c.set(Calendar.MILLISECOND, 0);
		} else if (periodType.equals("day")) {
			c.add(Calendar.DAY_OF_YEAR, 1);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.SECOND, 0);
	        c.set(Calendar.MILLISECOND, 0);
		}
		return c.getTimeInMillis();

	}
}
