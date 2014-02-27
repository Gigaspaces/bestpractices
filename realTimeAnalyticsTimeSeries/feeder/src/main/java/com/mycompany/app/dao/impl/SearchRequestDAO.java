package com.mycompany.app.dao.impl;

import net.jini.core.lease.Lease;

import com.mycompany.app.common.dao.ISearchRequestDAO;
import com.mycompany.app.common.domain.SearchRequest;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;

public class SearchRequestDAO implements ISearchRequestDAO {

	@GigaSpaceContext(name = "gigaSpace")
	private GigaSpace gigaSpace;
	
	public void setGigaSpace(GigaSpace gigaSpace) {
		this.gigaSpace = gigaSpace;
	}
	
	public void save(SearchRequest searchRequest) {
		gigaSpace.write(searchRequest, Lease.FOREVER);
	}
	
}