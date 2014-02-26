package com.mycompany.app.common.domain;

import com.mycompany.app.common.utils.Constants;
 
public class SearchRequest extends Request {

	private static final long serialVersionUID = -9091988609595094133L;
	
	private String type = Constants.TYPE_SEARCH;
    
    public SearchRequest() {}
    
    public SearchRequest(String rawData) {
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