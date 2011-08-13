package org.openspaces.ece.client.builders;

import org.openspaces.ece.client.ECEClient;
import org.openspaces.ece.client.clients.ECEExecutorClient;
import org.openspaces.ece.client.clients.ECEMasterWorkerClient;
import org.openspaces.ece.client.temp.Client;
import org.openspaces.ece.client.temp.ExecutorClient;
import org.openspaces.ece.client.temp.MasterWorkerClient;

public enum ClientType {
	MASTERWORKER(ECEMasterWorkerClient.class), EXECUTOR(ECEExecutorClient.class);
	private final Class<? extends ECEClient> clazz;

	ClientType(Class<? extends ECEClient> clazz) {
		this.clazz = clazz;
	}

    <T extends ECEClient> T build() throws InstantiationException, IllegalAccessException {
		return (T)clazz.newInstance();
	}

}