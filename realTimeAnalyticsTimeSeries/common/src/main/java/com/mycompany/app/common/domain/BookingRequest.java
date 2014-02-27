package com.mycompany.app.common.domain;

import com.mycompany.app.common.utils.Constants;

public class BookingRequest extends Request {

	private static final long serialVersionUID = 6002720966197737779L;
	
	private String type = Constants.TYPE_BOOKING;
	
	public BookingRequest() {}
	
	public BookingRequest(String rawData) {
    	this.setRawData(rawData);
        this.setProcessed(false);
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}