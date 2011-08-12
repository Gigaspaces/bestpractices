package org.openspaces.ece.client.swing;

public enum ClientType {
	MASTERWORKER(MasterWorkerClient.class), EXECUTOR(ExecutorClient.class);
	private final Class<? extends Client> clazz;

	ClientType(Class<? extends Client> clazz) {
		this.clazz = clazz;
	}

	Client build() throws InstantiationException, IllegalAccessException {
		return clazz.newInstance();
	}

}