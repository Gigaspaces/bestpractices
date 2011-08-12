package org.openspaces.ece.client.factories;

import org.openspaces.ece.client.ClientLogger;

public class ClientLoggerFactory {
    ClientLoggerType type;

    public ClientLoggerFactory swing() {
        type = ClientLoggerType.SWING;
        return this;
    }

    ClientLoggerFactory console() {
        type = ClientLoggerType.CONSOLE;
        return this;
    }

    public <T extends ClientLogger> T build() {
        ClientLogger logger = type.build();
        return (T)logger;
    }
}
