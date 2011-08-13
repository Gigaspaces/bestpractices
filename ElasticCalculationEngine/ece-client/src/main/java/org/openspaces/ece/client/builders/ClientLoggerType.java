package org.openspaces.ece.client.builders;

import org.openspaces.ece.client.ClientLogger;
import org.openspaces.ece.client.console.ConsoleLogger;
import org.openspaces.ece.client.swing.LogPanel;

public enum ClientLoggerType {
    SWING(LogPanel.class), CONSOLE(ConsoleLogger.class);
    Class<? extends ClientLogger> clazz;
    ClientLoggerType(Class<? extends ClientLogger> t) {
        clazz=t;
    }
    ClientLogger build() {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
