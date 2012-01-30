package org.openspaces.calcengine.common;

import java.io.Serializable;

public class CashFlowData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2243101085331430888L;
	private Double cashFlowYear0;
	private Double cashFlowYear1;
	private Double cashFlowYear2;
	private Double cashFlowYear3;
	private Double cashFlowYear4;
	private Double cashFlowYear5;

	public CashFlowData() {
	}

	public CashFlowData(double[] cashFlow) {
		cashFlowYear0 = cashFlow[0];
		cashFlowYear1 = cashFlow[1];
		cashFlowYear2 = cashFlow[2];
		cashFlowYear3 = cashFlow[3];
		cashFlowYear4 = cashFlow[4];
		cashFlowYear5 = cashFlow[5];
	}

	public Double getCashFlowYear0() {
		return cashFlowYear0;
	}

	public void setCashFlowYear0(Double cashFlowYear0) {
		this.cashFlowYear0 = cashFlowYear0;
	}

	public Double getCashFlowYear1() {
		return cashFlowYear1;
	}

	public void setCashFlowYear1(Double cashFlowYear1) {
		this.cashFlowYear1 = cashFlowYear1;
	}

	public Double getCashFlowYear2() {
		return cashFlowYear2;
	}

	public void setCashFlowYear2(Double cashFlowYear2) {
		this.cashFlowYear2 = cashFlowYear2;
	}

	public Double getCashFlowYear3() {
		return cashFlowYear3;
	}

	public void setCashFlowYear3(Double cashFlowYear3) {
		this.cashFlowYear3 = cashFlowYear3;
	}

	public Double getCashFlowYear4() {
		return cashFlowYear4;
	}

	public void setCashFlowYear4(Double cashFlowYear4) {
		this.cashFlowYear4 = cashFlowYear4;
	}

	public Double getCashFlowYear5() {
		return cashFlowYear5;
	}

	public void setCashFlowYear5(Double cashFlowYear5) {
		this.cashFlowYear5 = cashFlowYear5;
	}

	public double[] getCashFlowData() {
		return new double[] { cashFlowYear0, cashFlowYear1, cashFlowYear2,
				cashFlowYear3, cashFlowYear4, cashFlowYear5 };
	}
}
