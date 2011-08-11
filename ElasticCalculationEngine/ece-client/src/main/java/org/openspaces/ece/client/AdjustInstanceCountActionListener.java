package org.openspaces.ece.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;

public class AdjustInstanceCountActionListener implements ActionListener {
	StatefulPUPanel panel;

	public AdjustInstanceCountActionListener(StatefulPUPanel statefulPUPanel) {
		panel = statefulPUPanel;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				ProcessingUnits pus = panel.getAdminContainer().getAdmin()
						.getProcessingUnits();
				ProcessingUnit pu = pus.waitFor(panel.getProcessingUnitName(),
						1, TimeUnit.SECONDS);
				JButton button = (JButton) e.getSource();
				if (button.getText().equals("Add")) {
					pu.incrementInstance();
				} else {
					pu.decrementInstance();
				}
			}
		}).start();
	}

}
