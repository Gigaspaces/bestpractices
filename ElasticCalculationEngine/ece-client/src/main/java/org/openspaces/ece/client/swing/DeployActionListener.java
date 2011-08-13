package org.openspaces.ece.client.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;

import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.ProcessingUnits;

public class DeployActionListener implements ActionListener {
	StatefulPUPanel panel;

	public DeployActionListener(StatefulPUPanel panel) {
		this.panel = panel;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				JButton button = (JButton) e.getSource();
				if (button.getText().equals("Undeploy")) {
					if (panel.getAdminContainer() != null
							&& panel.getAdminContainer().getGSA() != null) {
						ProcessingUnits pus = panel.getAdminContainer()
								.getAdmin().getProcessingUnits();
						ProcessingUnit pu = pus.waitFor(
								panel.getProcessingUnitName(), 1,
								TimeUnit.SECONDS);
						pu.undeploy();
					}
				} else {
					// if it's not a stateful processing unit, just deploy it
					if (panel instanceof StatelessPUPanel) {
						staticDeploy();
					} else {
						// we need to determine elastic deployment or not, I
						// guess
						staticDeploy();
					}
				}
			}
		});
		t.start();
	}

	private void staticDeploy() {
		ensureInstances(4);
		GridServiceManager gsm = panel.getAdminContainer().getAdmin()
				.getGridServiceManagers()
				.waitForAtLeastOne(1, TimeUnit.SECONDS);
        ContainsResources resourceContainer=panel.getResourceContainer();
        Map<String, File> resources=resourceContainer.getResources();
        System.out.println(panel.getProcessingUnitName()+".jar");
        System.out.println(resources.get(panel.getProcessingUnitName()+".jar"));
        File processingUnitFile=resources.get(panel.getProcessingUnitName()+".jar");
		gsm.deploy(new ProcessingUnitDeployment(processingUnitFile));
	}

	private void ensureInstances(int i) {
		GridServiceContainers gscs = panel.getAdminContainer().getAdmin()
				.getGridServiceContainers();
		GridServiceAgents agents = panel.getAdminContainer().getAdmin()
				.getGridServiceAgents();
		agents.waitForAtLeastOne(1, TimeUnit.SECONDS);
		panel.getLogger().log("retrieved %d gscs",gscs.getSize());
		int gscsToStart = Math.max(0, i - gscs.getSize());
		int index = 0;

		while (gscsToStart != 0) {
			panel.getLogger().log("We need to start %d", gscsToStart);
			panel.getLogger().log("Starting GSC on agent %d", index);
			agents.getAgents()[index].startGridServiceAndWait(
					new GridServiceContainerOptions(), 30, TimeUnit.SECONDS);
			index = (index + 1) % agents.getSize();
			gscsToStart--;
		}
		panel.getLogger().log("We now have enough GSCs");
	}
}
