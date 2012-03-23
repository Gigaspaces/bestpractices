package com.gigaspaces.server;

import java.io.Serializable;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceRouting;

/**
 * This class represents an application domain object
 * 
 * @author dfilppi
 *
 */

@SuppressWarnings("serial")
@SpaceClass
public class DomainObject implements Serializable{
	private Integer id;
	private Integer value;
	private String site;
	private Long time;
	private Integer routingId;
	
	public DomainObject(){}
	
	public DomainObject(Integer id,Integer routingId, Integer value, String site){
		this.id=id;
		this.routingId=routingId;
		this.value=value;
		this.site=site;
	}
	
	public Integer getValue() {
		return value;
	}
	public void setValue(Integer value) {
		this.value = value;
	}
	
	public String getSite() {
		return site;
	}
	public void setSite(String site) {
		this.site = site;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	@SpaceRouting
	public void setRoutingId(Integer routingId) {
		this.routingId = routingId;
	}

	public Integer getRoutingId() {
		return routingId;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}
}
