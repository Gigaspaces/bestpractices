package org.openspaces.wan.demo.client;

import org.openspaces.core.GigaSpace;

import com.beust.jcommander.Parameter;

public class Configuration {
	@Parameter(names = { "-g" }, description = "The group to use to look up GSMs, default 'one'")
	String lookupGroup = "ONE";
	GigaSpace space;

	public String getLookupGroup() {
		return lookupGroup;
	}

	public void setLookupGroup(String lookupGroup) {
		this.lookupGroup = lookupGroup;
	}

	public GigaSpace getSpace() {
		return space;
	}

	public void setSpace(GigaSpace space) {
		this.space = space;
	}

}
