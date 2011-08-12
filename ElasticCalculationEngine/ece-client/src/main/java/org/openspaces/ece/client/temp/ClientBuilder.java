package org.openspaces.ece.client.temp;

public class ClientBuilder {
	private String url;
	private int trades;
	private int iterations;

	ClientType type;

	public ClientBuilder() {
	}

	ClientBuilder spaceUrl(String url) {
		this.url = url;
		return this;
	}

	ClientBuilder trades(int trades) {
		this.trades = trades;
		return this;
	}

	ClientBuilder iterations(int iterations) {
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

	Client build() {
		try {
			Client base = type.build();
			base.setIterations(iterations);
			base.setTrades(trades);
			base.setSpaceUrl(url);
			return base;
		} catch (Throwable e) {
			throw new Error(e);
		}
	}
}
