package org.openspaces.ece.client.swing;

import org.openspaces.core.GigaSpace;
import org.springframework.transaction.PlatformTransactionManager;

public class AbstractClient implements Client {
	String spaceUrl;

	GigaSpace space;

	PlatformTransactionManager txManager;

	int trades;
	int iterations;

	/* (non-Javadoc)
	 * @see org.openspaces.ece.client.swing.Client#getTrades()
	 */
	@Override
	public int getTrades() {
		return trades;
	}

	/* (non-Javadoc)
	 * @see org.openspaces.ece.client.swing.Client#setTrades(int)
	 */
	@Override
	public void setTrades(int trades) {
		this.trades = trades;
	}

	/* (non-Javadoc)
	 * @see org.openspaces.ece.client.swing.Client#getIterations()
	 */
	@Override
	public int getIterations() {
		return iterations;
	}

	/* (non-Javadoc)
	 * @see org.openspaces.ece.client.swing.Client#setIterations(int)
	 */
	@Override
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	@Override
	public String getSpaceUrl() {
		return spaceUrl;
	}

	@Override
	public void setSpaceUrl(String spaceUrl) {
		this.spaceUrl = spaceUrl;
	}

	public GigaSpace getSpace() {
		return space;
	}

	public void setSpace(GigaSpace space) {
		this.space = space;
	}

	public PlatformTransactionManager getTxManager() {
		return txManager;
	}

	public void setTxManager(PlatformTransactionManager txManager) {
		this.txManager = txManager;
	}

}
