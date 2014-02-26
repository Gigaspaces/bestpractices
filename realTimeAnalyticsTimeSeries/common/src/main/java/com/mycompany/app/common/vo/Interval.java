package com.mycompany.app.common.vo;

public class Interval implements java.io.Serializable {

	private static final long serialVersionUID = 6305454327456921962L;
	
	private Integer intervalNo;
	private Integer count;
	
	public Interval() {};
	
	public Interval(Integer intervalNo, Integer count) {
		this.intervalNo = intervalNo;
		this.count = count;
	};
	
	public Integer getIntervalNo() {
		return intervalNo;
	}
	
	public void setIntervalNo(Integer intervalNo) {
		this.intervalNo = intervalNo;
	}
	
	public Integer getCount() {
		return count;
	}
	
	public void setCount(Integer count) {
		this.count = count;
	}
	
}