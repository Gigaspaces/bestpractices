package org.openspaces.timeseries.shared;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class Tick implements Serializable{
	private String symbol;
	private String type;
	private Long time;
	private List<String> value;
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public List<String> getValue() {
		return value;
	}
	public void setValue(List<String> value) {
		this.value = value;
	}
	
	public String toString(){
		String s= symbol+","+type+","+String.valueOf(time);
		if(value!=null && value.size()>0)s+=","+value.get(0);
		return s;
	}
}
