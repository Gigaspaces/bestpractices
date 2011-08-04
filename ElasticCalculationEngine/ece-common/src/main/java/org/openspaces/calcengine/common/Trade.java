package org.openspaces.calcengine.common;

import java.io.Serializable;
import java.util.Arrays;

import com.gigaspaces.annotation.pojo.*;

@SpaceClass (persist=true)
public class Trade implements Serializable{

	public Trade (){}
	private Integer id;
	private Double NPV;
	private CashFlowData cashFlowData;
	
	@SpaceRouting
	@SpaceId (autoGenerate = false)
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Double getNPV(){
		return this.NPV;
	}
	
	public String getBook(){
		return "Book" + this.getId() % 4;
	}

	public void setNPV(Double nPV) {
		NPV = nPV;
	}

	public CashFlowData getCashFlowData() {
		if (cashFlowData == null )
			return new CashFlowData();
		
		return cashFlowData;
	}

	public void setCashFlowData(CashFlowData cashFlowData) {
		this.cashFlowData = cashFlowData;
	}

    public void setCashFlowData(double[] cashFlow) {
        CashFlowData data=new CashFlowData(cashFlow);
        setCashFlowData(data);
    }
}