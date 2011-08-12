package org.openspaces.ece.client.console;

import org.openspaces.ece.client.ClientLogger;

public class ConsoleLogger implements ClientLogger {
    @Override
    public void log(String format, Object... args) {
        System.out.printf(format, args);
        System.out.println();
    }
}
