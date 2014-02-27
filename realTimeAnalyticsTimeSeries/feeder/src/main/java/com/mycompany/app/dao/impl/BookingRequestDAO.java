package com.mycompany.app.dao.impl;

import com.mycompany.app.common.dao.IBookingRequestDAO;
import com.mycompany.app.common.domain.BookingRequest;

import net.jini.core.lease.Lease;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;

public class BookingRequestDAO implements IBookingRequestDAO {

	@GigaSpaceContext(name = "gigaSpace")
	private GigaSpace gigaSpace;
	
	public void setGigaSpace(GigaSpace gigaSpace) {
		this.gigaSpace = gigaSpace;
	}
	
	public void save(BookingRequest bookingRequest) {
		gigaSpace.write(bookingRequest, Lease.FOREVER);
	}
	
}