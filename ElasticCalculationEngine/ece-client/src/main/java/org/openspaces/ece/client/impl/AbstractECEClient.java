package org.openspaces.ece.client.impl;

import java.text.DecimalFormat;
import java.util.logging.Logger;

import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.core.GigaSpace;
import org.openspaces.ece.client.ECEClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

public abstract class AbstractECEClient implements ECEClient,
		ProcessingUnitInstanceLifecycleEventListener {
	Logger logger = Logger.getLogger(this.getClass().getName());
	DecimalFormat formatter = new DecimalFormat("0.0");
	boolean valid = true;
	@Autowired
	PlatformTransactionManager ptm = null;
	@Autowired
	GigaSpace space;

	public PlatformTransactionManager getPtm() {
		return ptm;
	}

	public void setPtm(PlatformTransactionManager ptm) {
		this.ptm = ptm;
	}

	public GigaSpace getSpace() {
		return space;
	}

	public void setSpace(GigaSpace space) {
		this.space = space;
	}

	int maxTrades = 10000;
	int maxIterations = 100;

	public boolean isValid() {
		return valid;
	}

	@Override
	public int getMaxTrades() {
		return maxTrades;
	}

	@Override
	public void setMaxTrades(int maxTrades) {
		this.maxTrades = maxTrades;
	}

	@Override
	public int getMaxIterations() {
		return maxIterations;
	}

	@Override
	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}
}
