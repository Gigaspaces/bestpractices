package org.openspaces.ece.client.temp;

public interface Client {

	public abstract int getTrades();

	public abstract void setTrades(int trades);

	public abstract int getIterations();

	public abstract void setIterations(int iterations);

	public abstract void setSpaceUrl(String spaceUrl);

	public abstract String getSpaceUrl();

}