package org.openspaces.ece.client.builders;

import org.openspaces.ece.client.ECEClient;
import org.openspaces.ece.client.clients.ECEExecutorClient;
import org.openspaces.ece.client.clients.ECEMasterWorkerClient;

public enum ClientType {
    MASTERWORKER(ECEMasterWorkerClient.class), EXECUTOR(ECEExecutorClient.class);
    private final Class<? extends ECEClient> clazz;

    ClientType(Class<? extends ECEClient> clazz) {
        this.clazz = clazz;
    }

    <T extends ECEClient> T build() throws InstantiationException, IllegalAccessException {
        //noinspection unchecked
        return (T) clazz.newInstance();
    }

}