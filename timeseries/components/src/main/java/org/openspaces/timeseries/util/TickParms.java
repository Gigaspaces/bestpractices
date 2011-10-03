package org.openspaces.timeseries.util;

import java.io.Serializable;

public class TickParms implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String symbol;
	private int minvol,maxvol;
	private double priceBasis;
	private double lastprice;
	private double lastbid;
	private double lastask;
	private int lastbidvol;
	private int lastaskvol;
	
	public TickParms(){}
	
	public TickParms(String symbol, int minvol, int maxvol,
			double priceBasis) {
		super();
		this.symbol = symbol;
		this.minvol = minvol;
		this.maxvol = maxvol;
		this.priceBasis = priceBasis;
		this.lastprice=priceBasis;
		this.lastbid=this.lastprice-5;
		this.lastask=this.lastprice+5;
		lastbidvol=(minvol+maxvol)/2;
		lastaskvol=lastbidvol;  //dumb defaults
	}
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public int getMinvol() {
		return minvol;
	}
	public void setMinvol(int minvol) {
		this.minvol = minvol;
	}
	public int getMaxvol() {
		return maxvol;
	}
	public void setMaxvol(int maxvol) {
		this.maxvol = maxvol;
	}
	public double getPriceBasis() {
		return priceBasis;
	}
	public void setPriceBasis(double priceBasis) {
		this.priceBasis = priceBasis;
		this.lastprice=priceBasis;
		this.lastbid=this.lastprice-.05;
		this.lastask=this.lastprice+.05;
	}

	public double getLastprice() {
		return lastprice;
	}

	public void setLastprice(double lastprice) {
		this.lastprice = lastprice;
	}

	public double getLastbid() {
		return lastbid;
	}

	public void setLastbid(double lastbid) {
		this.lastbid = lastbid;
	}

	public double getLastask() {
		return lastask;
	}

	public void setLastask(double lastask) {
		this.lastask = lastask;
	}

	public int getLastbidvol() {
		return lastbidvol;
	}

	public void setLastbidvol(int lastbidvol) {
		this.lastbidvol = lastbidvol;
	}

	public int getLastaskvol() {
		return lastaskvol;
	}

	public void setLastaskvol(int lastaskvol) {
		this.lastaskvol = lastaskvol;
	}
	
	public String toString(){
		return String.format("%s %d %d %f",symbol,minvol,maxvol,priceBasis);
	}

}
