package org.openspaces.ece.client.swing;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;

public class StatefulPUWatcher extends TimerTask {
	StatefulPUPanel panel;

	public StatefulPUWatcher(StatefulPUPanel panel) {
		this.panel = panel;
	}

	public void getData(int[] counts) {
		if (panel.getAdminContainer() != null
				&& panel.getAdminContainer().getGSA() != null) {
			ProcessingUnits pus = panel.getAdminContainer().getAdmin()
					.getProcessingUnits();
			ProcessingUnit pu = pus.waitFor(panel.getProcessingUnitName(), 1,
					TimeUnit.SECONDS);
			if (pu != null) {
				counts[0] = pu.getNumberOfInstances();
				counts[1] = pu.getNumberOfBackups();
				counts[2] = pu.getTotalNumberOfInstances();
				counts[3] = pu.canIncrementInstance() ? 1 : 0;
				counts[4] = pu.canDecrementInstance() ? 1 : 0;
			}
		}

	}

	@Override
	public final void run() {
		final int[] counts = { 0, 0, 0, 0, 0 };
		getData(counts);
		final boolean isDeployEnabled = isDeployEnabled();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				panel.txtPUName.setText(panel.getProcessingUnitName());
				panel.txtPrimaries.setText(String.valueOf(counts[0]));
				panel.txtBackups.setText(String.valueOf(counts[1]));
				panel.txtTotal.setText(String.valueOf(counts[2]));
				panel.btnDeploy.setEnabled(isDeployEnabled);
				if (panel.getAdminContainer().getGSA() != null) {
					panel.btnDeploy.setText(counts[0] > 0 ? "Undeploy"
							: "Deploy");
					panel.btnRemoveInstance.setEnabled(counts[3] == 1
							&& counts[0] > 1);
					panel.btnAddInstance.setEnabled(counts[4] == 1
							&& counts[0] > 0);
				} else {
					panel.btnAddInstance.setEnabled(false);
					panel.btnRemoveInstance.setEnabled(false);
				}

			}

		});
	}

	boolean isDeployEnabled() {
		return panel.getAdminContainer().getGSA() != null;
	}

}
