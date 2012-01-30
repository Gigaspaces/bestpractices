package org.openspaces.ece.client;

import org.openspaces.ece.client.clients.NoWorkersAvailableException;

public interface ECEClient {
    void issueTrades();

    int getMaxTrades();

    void setMaxTrades(int maxTrades);

    int getMaxIterations();

    void setMaxIterations(int maxIterations);

    boolean isValid();

    void setClientLogger(ClientLogger logger);

    void setSpaceUrl(String spaceUrl);

    void init() throws NoWorkersAvailableException;
    void setContainsAdmin(ContainsAdmin admin);

    String getGroup();

    void setGroup(String group);

    String getLocator();

    void setLocator(String locator);
}
