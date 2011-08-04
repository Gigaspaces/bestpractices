package org.openspaces.ece.client;

public interface ECEClient {
    void issueTrades();

    int getMaxTrades();

    void setMaxTrades(int maxTrades);

    int getMaxIterations();

    void setMaxIterations(int maxIterations);

    boolean isValid();

}
