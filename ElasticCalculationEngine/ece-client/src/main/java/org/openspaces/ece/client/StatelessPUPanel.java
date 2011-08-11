package org.openspaces.ece.client;

public class StatelessPUPanel extends StatefulPUPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8223766446356642796L;
	String statefulName;

	public String getStatefulName() {
		return statefulName;
	}

	public void setStatefulName(String statefulName) {
		this.statefulName = statefulName;
	}

}
