package com.mycompany.app.common.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;

import com.mycompany.app.common.domain.BookingTimeSeries;
import com.mycompany.app.common.vo.Airline;
import com.mycompany.app.common.vo.Interval;
import com.mycompany.app.common.vo.SourceDestinationAirports;

public class AirportDataUtils {
	
	private AirportDataUtils() {}
	
	public static Map<Integer, SourceDestinationAirports> sourceDestinationMap = new HashMap<Integer, SourceDestinationAirports>();
	public static Map<String, Integer> sourceDestinationReferenceIdMap = new HashMap<String, Integer>();
    
	//Initialize maps with random source and destinations airports
	static {
    	sourceDestinationMap.put(0, new SourceDestinationAirports(0, "FAI", "JFK"));
    	sourceDestinationMap.put(1, new SourceDestinationAirports(1, "BHM", "DHN"));
    	sourceDestinationMap.put(2, new SourceDestinationAirports(2, "HSV", "MOB"));
    	sourceDestinationMap.put(3, new SourceDestinationAirports(3, "MGM", "ANI"));
    	sourceDestinationMap.put(4, new SourceDestinationAirports(4, "MRI", "ENM"));
    	
    	for(Integer i : sourceDestinationMap.keySet()) {
    		SourceDestinationAirports airports = sourceDestinationMap.get(i);
    		String key = generateSourceDestinationKey(airports.getSourceAirport(), airports.getDesintationAirport());
    		sourceDestinationReferenceIdMap.put(key, i);
    	}
    }
	 
    //Random acronyms representing different Airlines
	private static String AIR_LINES[] = {"AA", "BA", "CP", "HA"};
	
	private static Random random = new Random();
		
	public static String generateSourceDestinationKey(String source, String destination) {
		return source + Constants.DASH + destination;
	}
	
	public static String generateRandomAirport() {
		return AIR_LINES[random.nextInt(AIR_LINES.length)];
	}
	
	public static SourceDestinationAirports generateRandomSourceDestinationAirPorts() {
		return sourceDestinationMap.get(random.nextInt(sourceDestinationMap.size()));
	}
	
	public static void updateAirportMap(BookingTimeSeries bookingTimeSeries, SortedMap<String, SourceDestinationAirports> airports) {
    	Map<String, Integer> sourceDestinationMap = bookingTimeSeries.getSourceDestinationCounter();
		for(String sourceDestination : sourceDestinationMap.keySet()) {
			
			SourceDestinationAirports sourceDestinationAirports = null;
			if(!airports.containsKey(sourceDestination)) {
				airports.put(sourceDestination, new SourceDestinationAirports(AirportDataUtils.sourceDestinationReferenceIdMap.get(sourceDestination)));
			}
			sourceDestinationAirports = airports.get(sourceDestination);
			
			String airlineName = bookingTimeSeries.getAirline();
			SortedMap<String, Airline> airlines = sourceDestinationAirports.getAirlines();
			Airline airline = null;
			if(!airlines.containsKey(airlineName)) {
				airlines.put(airlineName, new Airline(airlineName));
			}
			airline = airlines.get(airlineName);
							
			Integer intervalNo = bookingTimeSeries.getInterval();
			SortedMap<Integer, Interval> intervals = airline.getIntervals();
			if(!intervals.containsKey(intervalNo)) {
				intervals.put(intervalNo, new Interval(intervalNo, sourceDestinationMap.get(sourceDestination)));
			}
		}
	}

}