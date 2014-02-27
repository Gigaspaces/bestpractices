package com.mycompany.app.common.domain;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;

@SpaceClass 
public abstract class Request implements java.io.Serializable {

	private static final long serialVersionUID = -8041464564465933524L;
	
	private String id;
    private String airline;
    private Integer sourceDestinationId; 
    private String sourceAirport;
    private String destinationAirport;
    private String rawData;
    private String processedData;
    private Boolean processed;
    
    public Request() {}

    @SpaceId(autoGenerate=true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    @SpaceRouting
    public String getAirline() {
		return airline;
	}

	public void setAirline(String airline) {
		this.airline = airline;
	}

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public String getProcessedData() {
		return processedData;
	}

	public void setProcessedData(String processedData) {
		this.processedData = processedData;
	}

	public Boolean isProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

	public Integer getSourceDestinationId() {
		return sourceDestinationId;
	}

	public void setSourceDestinationId(Integer sourceDestinationId) {
		this.sourceDestinationId = sourceDestinationId;
	}

	public String getSourceAirport() {
		return sourceAirport;
	}

	public void setSourceAirport(String sourceAirport) {
		this.sourceAirport = sourceAirport;
	}

	public String getDestinationAirport() {
		return destinationAirport;
	}

	public void setDestinationAirport(String destinationAirport) {
		this.destinationAirport = destinationAirport;
	}

	public String toString() {
        return "id[" + id + "] airline[" + airline + "] rawData[" + rawData + "] data[" + processedData + "] processed[" + processed + "]";
    }
    
}