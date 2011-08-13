package org.openspaces.ece.client.builders;

import org.openspaces.ece.client.ClientLogger;

public class ClientLoggerBuilder {
    ClientLoggerType type;

    public ClientLoggerBuilder swing() {
        type = ClientLoggerType.SWING;
        return this;
    }

    public ClientLoggerBuilder console() {
        type = ClientLoggerType.CONSOLE;
        return this;
    }

    public <T extends ClientLogger> T build() {
        ClientLogger logger = type.build();
        return (T)logger;
    }
}
