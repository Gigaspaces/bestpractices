package org.openspaces.ece.client;

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;

public class StatelessPUWatcher extends StatefulPUWatcher {
	StatelessPUPanel panel;

	public StatelessPUWatcher(StatelessPUPanel panel) {
		super(panel);
		this.panel = panel;
	}

	ProcessingUnit pu;

	@Override
	public void getData(int[] counts) {
		if (panel.getAdminContainer() != null
				&& panel.getAdminContainer().getGSA() != null) {
			ProcessingUnits pus = panel.getAdminContainer().getAdmin()
					.getProcessingUnits();
			pu = pus.waitFor(panel.getStatefulName(), 1, TimeUnit.SECONDS);
			if (pu != null) {
				super.getData(counts);
			}
		}
	}

	boolean isDeployEnabled() {
		return pu != null;
	}
}
