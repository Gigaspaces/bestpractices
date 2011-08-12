package org.openspaces.ece.client.swing;

import org.openspaces.ece.client.ClientLogger;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class StatefulPUPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4286254361424800507L;
	String processingUnitName;
	ContainsAdmin adminContainer;
	ContainsResources resourceContainer;
	ClientLogger logger;
	JTextField txtPUName;
	JTextField txtPrimaries;
	JTextField txtBackups;
	JTextField txtTotal;
	JButton btnDeploy;
	JButton btnAddInstance;
	JButton btnRemoveInstance;

	public ContainsAdmin getAdminContainer() {
		return adminContainer;
	}

	public void setAdminContainer(ContainsAdmin adminContainer) {
		this.adminContainer = adminContainer;
	}

	public ContainsResources getResourceContainer() {
		return resourceContainer;
	}

	public void setResourceContainer(ContainsResources resourceContainer) {
		this.resourceContainer = resourceContainer;
	}

	public ClientLogger getLogger() {
		return logger;
	}

	public void setLogger(ClientLogger logger) {
		this.logger = logger;
	}

	public String getProcessingUnitName() {
		return processingUnitName;
	}

	public void setProcessingUnitName(String processingUnitName) {
		this.processingUnitName = processingUnitName;
	}

	public StatefulPUPanel(String processingUnitName,
			ContainsAdmin adminContainer, ContainsResources resourceContainer,
			ClientLogger logger) {
		super();
		this.processingUnitName = processingUnitName;
		this.adminContainer = adminContainer;
		this.resourceContainer = resourceContainer;
		this.logger = logger;
	}

	/**
	 * Create the panel.
	 */
	public StatefulPUPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 120, 30, 30, 30, 67, 53, 73, 0 };
		gridBagLayout.rowHeights = new int[] { 23, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 1.0, 1.0, 0.0,
				0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		txtPUName = new JTextField();
		GridBagConstraints gbc_txtPUName = new GridBagConstraints();
		gbc_txtPUName.insets = new Insets(0, 0, 0, 5);
		gbc_txtPUName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPUName.gridx = 0;
		gbc_txtPUName.gridy = 0;
		add(txtPUName, gbc_txtPUName);
		txtPUName.setColumns(10);

		txtPrimaries = new JTextField();
		GridBagConstraints gbc_txtPrimaries = new GridBagConstraints();
		gbc_txtPrimaries.insets = new Insets(0, 0, 0, 5);
		gbc_txtPrimaries.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPrimaries.gridx = 1;
		gbc_txtPrimaries.gridy = 0;
		add(txtPrimaries, gbc_txtPrimaries);
		txtPrimaries.setColumns(10);

		txtBackups = new JTextField();
		GridBagConstraints gbc_txtBackups = new GridBagConstraints();
		gbc_txtBackups.insets = new Insets(0, 0, 0, 5);
		gbc_txtBackups.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtBackups.gridx = 2;
		gbc_txtBackups.gridy = 0;
		add(txtBackups, gbc_txtBackups);
		txtBackups.setColumns(10);

		txtTotal = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 3;
		gbc_textField.gridy = 0;
		add(txtTotal, gbc_textField);
		txtTotal.setColumns(10);

		btnDeploy = new JButton("Deploy") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -5437658310662730549L;

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(120, (int) super.getPreferredSize()
						.getHeight());
			}

			@Override
			public Dimension getMaximumSize() {
				return getPreferredSize();
			}

			@Override
			public Dimension getMinimumSize() {
				return getPreferredSize();
			}

		};
		btnDeploy.addActionListener(new DeployActionListener(this));
		GridBagConstraints gbc_btnDeploy = new GridBagConstraints();
		gbc_btnDeploy.insets = new Insets(0, 0, 0, 5);
		gbc_btnDeploy.gridx = 4;
		gbc_btnDeploy.gridy = 0;
		add(btnDeploy, gbc_btnDeploy);

		btnAddInstance = new JButton("Add");
		btnAddInstance.addActionListener(new AdjustInstanceCountActionListener(
				this));
		GridBagConstraints gbc_btnAddInstance = new GridBagConstraints();
		gbc_btnAddInstance.insets = new Insets(0, 0, 0, 5);
		gbc_btnAddInstance.gridx = 5;
		gbc_btnAddInstance.gridy = 0;
		add(btnAddInstance, gbc_btnAddInstance);

		btnRemoveInstance = new JButton("Remove");
		btnRemoveInstance
				.addActionListener(new AdjustInstanceCountActionListener(this));
		GridBagConstraints gbc_btnRemoveInstance = new GridBagConstraints();
		gbc_btnRemoveInstance.gridx = 6;
		gbc_btnRemoveInstance.gridy = 0;
		add(btnRemoveInstance, gbc_btnRemoveInstance);
	}
}
