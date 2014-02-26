package com.mycompany.app.common.vo;

import java.util.SortedMap;
import java.util.TreeMap;

public class SourceDestinationAirports implements java.io.Serializable {

	private static final long serialVersionUID = 199250933696367033L;
	
	private Integer id;
	private String sourceAirport;
	private String desintationAirport;
	private SortedMap<String, Airline> airlines;
	
	public SourceDestinationAirports() {}
	
	public SourceDestinationAirports(Integer id) {
		this.id = id;
	}
	
	public SourceDestinationAirports(Integer id, String s, String d) {
		this.id = id;
		this.sourceAirport = s;
		this.desintationAirport = d;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getSourceAirport() {
		return sourceAirport;
	}
	
	public void setSourceAirport(String sourceAirport) {
		this.sourceAirport = sourceAirport;
	}
	
	public String getDesintationAirport() {
		return desintationAirport;
	}
	
	public void setDesintationAirport(String desintationAirport) {
		this.desintationAirport = desintationAirport;
	}

	public SortedMap<String, Airline> getAirlines() {
		if(airlines == null)
			airlines = new TreeMap<String, Airline>();
		return airlines;
	}

	public void setAirlines(SortedMap<String, Airline> airlines) {
		this.airlines = airlines;
	}
	
}