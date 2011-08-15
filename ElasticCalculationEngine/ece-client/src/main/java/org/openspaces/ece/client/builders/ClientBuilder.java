package org.openspaces.ece.client.builders;

import org.openspaces.ece.client.*;
import org.openspaces.ece.client.ContainsAdmin;
import org.openspaces.ece.client.ContainsResources;

public class ClientBuilder {
    private String url;
    private int trades;
    private int iterations;
    private ClientType type;
    private ClientLogger logger;
    private String group;
    private String locator;
    private ContainsResources resourceContainer;
    private ContainsAdmin adminContainer;

    public ClientBuilder() {
    }

    public ClientBuilder logger(ClientLogger logger) {
        this.logger = logger;
        return this;
    }

    public ClientBuilder group(String group) {
        this.group = group;
        return this;
    }

    public ClientBuilder locator(String locator) {
        this.locator = locator;
        return this;
    }

    public ClientBuilder spaceUrl(String url) {
        this.url = url;
        return this;
    }

    public ClientBuilder trades(int trades) {
        this.trades = trades;
        return this;
    }

    public ClientBuilder iterations(int iterations) {
        this.iterations = iterations;
        return this;
    }

    ClientBuilder masterWorker() {
        type = ClientType.MASTERWORKER;
        return this;
    }

    ClientBuilder executor() {
        type = ClientType.EXECUTOR;
        return this;
    }

    public ClientBuilder type(String type) {
        this.type = ClientType.valueOf(type.toUpperCase());
        return this;
    }

    public ClientBuilder admin(ContainsAdmin adminContainer) {
        this.adminContainer = adminContainer;
        return this;
    }

    public ClientBuilder resource(ContainsResources resourceContainer) {
        this.resourceContainer = resourceContainer;
        return this;
    }

    public ECEClient build() {
        try {
            ECEClient base = type.build();
            base.setMaxIterations(iterations);
            base.setMaxTrades(trades);
            base.setSpaceUrl(url);
            if (locator != null) {
                base.setLocator(locator);
            }
            if (group != null) {
                base.setGroup(group);
            }
            base.setContainsAdmin(adminContainer);
            base.setClientLogger(logger);
            return base;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
}
