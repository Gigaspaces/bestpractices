package org.openspaces.ece.client.clients;

public class NoWorkersAvailableException extends Throwable {
    public NoWorkersAvailableException() {
    }

    public NoWorkersAvailableException(Throwable cause) {
        super(cause);
    }

    public NoWorkersAvailableException(String message) {
        super(message);
    }

    public NoWorkersAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
