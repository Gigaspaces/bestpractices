package org.openspaces.ece.client;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.esm.ElasticServiceManagers;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.ProcessingUnits;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class SwingClient {
	private final class StaticDataGridDeployTask implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			log("Submitting static deploy request for Data Grid");
			service.submit(new Runnable() {
				@Override
				public void run() {
					// need to add GSCs if we have more primary instances than
					// GSCs
					GridServiceContainers gscs = admin
							.getGridServiceContainers();
					log("retrieved gscs");
					int gscsToStart = Math.max(0, 4 - gscs.getSize());
					int index = 0;
					while (gscsToStart != 0) {
						log("Starting GSC on agent " + index);
						agents.getAgents()[index].startGridServiceAndWait(
								new GridServiceContainerOptions(), 30,
								TimeUnit.SECONDS);
						index = (index + 1) % agents.getSize();
						gscsToStart--;
					}

					dataGridPU = gsm.deploy(new ProcessingUnitDeployment(
							dataGridPUFileRef));
					btnDeployDataGridElastic.setEnabled(false);
					btnDeployDataGridStatic.setEnabled(false);
				}
			});
		}
	}

	private final class GigaSpacesAdminFinderTask implements Runnable {
		@Override
		public void run() {
			log("Searching for GigaSpaces Admin objects");
			admin = new AdminFactory().addGroup(group).addLocator(locator)
					.createAdmin();
			log("Admin: " + admin);
			agents = admin.getGridServiceAgents();
			agents.waitForAtLeastOne(10, TimeUnit.SECONDS);
			log("Agents: " + Arrays.toString(agents.getAgents()));
			gsm = admin.getGridServiceManagers().waitForAtLeastOne(10,
					TimeUnit.SECONDS);
			log("GSM: " + gsm);
			ElasticServiceManagers esms = admin.getElasticServiceManagers();
			log("ESMs: " + Arrays.toString(esms.getManagers()));
			esm = esms.waitForAtLeastOne(10, TimeUnit.SECONDS);
			log("ESM: " + esm);
			ProcessingUnits pus = admin.getProcessingUnits();

			log("looking for DataGrid");
			dataGridPU = pus.waitFor("ece-datagrid", 5, TimeUnit.SECONDS);
			log("DataGrid " + (dataGridPU == null ? "not " : "") + "found");

			log("Looking for worker processing units");
			workerPU = pus.waitFor("ece-worker", 5, TimeUnit.SECONDS);
			log("Worker processing units " + (workerPU == null ? "not " : "")
					+ "found");

			log("GigaSpaces Admin search done");
		}
	}

	public final class ArtifactFinderTask implements Runnable {
		@Override
		public void run() {
			log("Searching for file artifacts");
			String sep = System.getProperty("file.separator");
			String home = System.getProperty("user.dir");
			String dataGridFileName = home + sep + ".." + sep + "ece-datagrid"
					+ sep + "target" + sep + "ece-datagrid.jar";
			dataGridPUFileRef = new File(dataGridFileName);
			log(dataGridPUFileRef + " exists: "
					+ String.valueOf(dataGridPUFileRef.exists()));
			String workerPUFileName = home + sep + ".." + sep + "ece-worker"
					+ sep + "target" + sep + "ece-worker.jar";
			workerPUFileRef = new File(workerPUFileName);
			log(workerPUFileRef + " exists " + workerPUFileRef.exists());
			log("Artifact search done");
		}
	}

	@Parameter(names = { "-l", "--locator" })
	String locator = "127.0.0.1";
	@Parameter(names = { "-g", "--group" })
	String group = "Gigaspaces-XAPPremium-8.0.3-rc";
	@Parameter(names = { "-m" })
	Integer initialWorkers = 2;

	private JFrame frmElasticCalculationEngine;
	File dataGridPUFileRef;
	ProcessingUnit dataGridPU;
	ProcessingUnit workerPU;
	File workerPUFileRef;
	Admin admin;
	GridServiceAgents agents;
	GridServiceManager gsm;
	ElasticServiceManager esm;
	private JTextField textField;
	private JTextField textField_2;
	private JTextPane logPane = new JTextPane();
	JButton btnDeployWorkerDirect;
	JButton btnDeployDataGridElastic;
	JButton btnDeployDataGridStatic;
	JButton btnUndeployDataGrid;
	JButton btnUndeployWorker;
	ExecutorService service = Executors.newFixedThreadPool(2);
	JTextField txtDataGridInstances;
	JTextField txtWorkerInstances;
	java.util.Timer timerDataGrid = new java.util.Timer();
	java.util.Timer timerWorker = new java.util.Timer();
	JButton btnAddWorkerInstance;
	JButton btnRemoveWorkerInstance;
	JButton btnAddDataGridInstance;
	JButton btnRemoveDataGridInstance;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SwingClient window = new SwingClient();
					new JCommander(window, args);
					window.init();
					window.frmElasticCalculationEngine.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SwingClient() {
	}

	public void init() {
		initialize();
		service.submit(new ArtifactFinderTask());
		service.submit(new GigaSpacesAdminFinderTask());
		timerDataGrid.schedule(new TimerTask() {
			@Override
			public void run() {
				int count = 0;
				if (admin != null && dataGridPU != null) {
					count = dataGridPU.getInstances().length;
				}
				txtDataGridInstances.setText(String.valueOf(count));

				btnUndeployDataGrid.setEnabled(count > 0);
				btnDeployDataGridStatic.setEnabled(count == 0);
				btnDeployDataGridElastic.setEnabled(count == 0 && esm != null);
				btnAddDataGridInstance.setEnabled(dataGridPU != null
						&& dataGridPU.canIncrementInstance() && count > 0);
				btnAddDataGridInstance.setEnabled(dataGridPU != null
						&& dataGridPU.canDecrementInstance() && count > 1);
			}
		}, 0, 2000);

		timerWorker.schedule(new TimerTask() {
			@Override
			public void run() {
				int count = 0;
				if (admin != null && workerPU != null) {
					count = workerPU.getInstances().length;
				}
				txtWorkerInstances.setText(String.valueOf(count));
				btnDeployWorkerDirect.setEnabled(count == 0);
				btnUndeployWorker.setEnabled(count > 0);
				btnAddWorkerInstance.setEnabled(workerPU != null
						&& workerPU.canIncrementInstance() && count > 0);
				btnAddWorkerInstance.setEnabled(workerPU != null
						&& workerPU.canDecrementInstance() && count > 1);
			}
		}, 0, 2000);
	}

	void log(final String loggedEvent) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				logPane.setText(logPane.getText() + loggedEvent + "\n");
			}
		});

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmElasticCalculationEngine = new JFrame();
		frmElasticCalculationEngine
				.setTitle("Elastic Calculation Engine Client");
		frmElasticCalculationEngine.setBounds(100, 100, 750, 400);
		frmElasticCalculationEngine
				.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panelApplication = new JPanel();
		frmElasticCalculationEngine.getContentPane().add(panelApplication,
				BorderLayout.NORTH);
		panelApplication.setLayout(new BorderLayout(0, 0));

		JPanel panelDeploy = new JPanel();
		panelApplication.add(panelDeploy, BorderLayout.NORTH);

		JLabel lblDeployDatagrid = new JLabel("Deploy Datagrid:");
		panelDeploy.add(lblDeployDatagrid);

		btnDeployDataGridStatic = new JButton("Static");
		btnDeployDataGridStatic
				.addActionListener(new StaticDataGridDeployTask());
		btnDeployDataGridStatic.setEnabled(false);
		btnDeployDataGridStatic
				.setToolTipText("This deploys a static datagrid processing unit. Instance count is determined by the client.");
		panelDeploy.add(btnDeployDataGridStatic);

		btnDeployDataGridElastic = new JButton("Elastic");
		btnDeployDataGridElastic.setEnabled(false);
		btnDeployDataGridElastic
				.setToolTipText("This deploys an elastic data grid; instance count is determined by memory usage.");
		panelDeploy.add(btnDeployDataGridElastic);

		btnUndeployDataGrid = new JButton("Undeploy");
		btnUndeployDataGrid.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				log("Submitting undeploy request for Data Grid");
				service.submit(new Runnable() {
					@Override
					public void run() {
						dataGridPU.undeploy();
						dataGridPU = null;
					}
				});
			}
		});
		btnUndeployDataGrid.setEnabled(false);
		panelDeploy.add(btnUndeployDataGrid);

		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		panelDeploy.add(separator);

		JLabel lblNewLabel = new JLabel("Deploy Worker:");
		panelDeploy.add(lblNewLabel);

		btnDeployWorkerDirect = new JButton("Direct");
		btnDeployWorkerDirect
				.setToolTipText("This deploys a worker PU. Instance count is controlled by a client application.");
		btnDeployWorkerDirect.setEnabled(false);
		btnDeployWorkerDirect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				log("Submitting static deploy request for Worker");
				service.submit(new Runnable() {
					@Override
					public void run() {
						GridServiceContainers gscs = admin
								.getGridServiceContainers();
						log("retrieved gscs");
						int gscsToStart = Math.max(0, 1 - gscs.getSize());
						int index = 0;
						while (gscsToStart != 0) {
							log("Starting GSC on agent " + index);
							agents.getAgents()[index].startGridServiceAndWait(
									new GridServiceContainerOptions(), 30,
									TimeUnit.SECONDS);
							index = (index + 1) % agents.getSize();
							gscsToStart--;
						}

						workerPU = gsm.deploy(new ProcessingUnitDeployment(
								workerPUFileRef));
					}
				});
			}
		});
		panelDeploy.add(btnDeployWorkerDirect);

		btnUndeployWorker = new JButton("Undeploy");
		btnUndeployWorker.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				log("Submitting undeploy request for Worker");
				service.submit(new Runnable() {
					@Override
					public void run() {
						workerPU.undeploy();
						workerPU = null;
					}
				});
			}
		});
		panelDeploy.add(btnUndeployWorker);

		JPanel panelScale = new JPanel();
		frmElasticCalculationEngine.getContentPane().add(panelScale,
				BorderLayout.CENTER);
		panelScale.setLayout(new MigLayout("", "[][grow,left][grow][][][]",
				"[][][][grow,bottom]"));

		JLabel lblStatus = new JLabel("Status");
		panelScale.add(lblStatus, "cell 1 0");

		JLabel lblInstances = new JLabel("Instances");
		panelScale.add(lblInstances, "cell 2 0");

		JLabel lblDatagrid = new JLabel("Datagrid");
		lblDatagrid.setHorizontalAlignment(SwingConstants.CENTER);
		panelScale.add(lblDatagrid, "cell 0 1,growx");

		textField = new JTextField();
		textField.setHorizontalAlignment(SwingConstants.LEFT);
		panelScale.add(textField, "cell 1 1,growx");
		textField.setColumns(1);

		txtDataGridInstances = new JTextField();
		txtDataGridInstances.setHorizontalAlignment(SwingConstants.RIGHT);
		panelScale.add(txtDataGridInstances, "cell 2 1,growx");
		txtDataGridInstances.setColumns(10);

		btnAddDataGridInstance = new JButton("Add Instance");
		btnAddDataGridInstance.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dataGridPU.incrementInstance();
			}
		});
		panelScale.add(btnAddDataGridInstance, "cell 3 1");

		btnRemoveDataGridInstance = new JButton("Remove Instance");
		btnRemoveDataGridInstance.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dataGridPU.decrementInstance();
			}
		});
		panelScale.add(btnRemoveDataGridInstance, "cell 5 1");

		JLabel lblWorker = new JLabel("Worker");
		panelScale.add(lblWorker, "cell 0 2,alignx trailing");

		textField_2 = new JTextField();
		panelScale.add(textField_2, "cell 1 2,growx");
		textField_2.setColumns(10);

		txtWorkerInstances = new JTextField();
		txtWorkerInstances.setHorizontalAlignment(SwingConstants.RIGHT);
		panelScale.add(txtWorkerInstances, "cell 2 2,growx");
		txtWorkerInstances.setColumns(10);

		btnAddWorkerInstance = new JButton("Add Instance");
		btnAddWorkerInstance.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				workerPU.incrementInstance();
			}
		});
		panelScale.add(btnAddWorkerInstance, "cell 3 2");

		btnRemoveWorkerInstance = new JButton("Remove Instance");
		btnRemoveWorkerInstance.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				workerPU.decrementInstance();
			}
		});
		panelScale.add(btnRemoveWorkerInstance, "cell 5 2");

		JScrollPane scrollPane = new JScrollPane(logPane);
		logPane.setFont(new Font("Consolas", Font.PLAIN, 10));
		panelScale.add(scrollPane, "cell 0 3 6 1,grow");
	}
}
