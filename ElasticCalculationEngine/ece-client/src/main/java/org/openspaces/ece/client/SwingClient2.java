package org.openspaces.ece.client;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class SwingClient2 implements ContainsAdmin, ContainsResources {
	ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime()
			.availableProcessors());
	private JFrame frmElasticCalculationEngine;
	private LogPanel logPanel;
	@Parameter(names = { "-l", "--locator" })
	String locator = "127.0.0.1";
	@Parameter(names = { "-g", "--group" })
	String group = "Gigaspaces-XAPPremium-8.0.3-rc";
	@Parameter(names = { "-m" })
	Integer initialWorkers = 2;
	@Parameter(names = { "-h", "--gshome" })
	String gsHome = "C:\\tools\\Gigaspaces-XAP-Premium-8.0.3-rc";
	Map<String, File> resources = new ConcurrentHashMap<String, File>();
	Admin admin = null;
	GridServiceAgent GSA = null;
	List<Timer> timers = new ArrayList<Timer>();
	StatefulPUPanel dataGridPanel;
	StatelessPUPanel workerPanel;

	public void log(String format, Object... args) {
		logPanel.log(format, args);
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SwingClient2 window = new SwingClient2();
					new JCommander(window);
					window.initialize();
					window.frmElasticCalculationEngine.setVisible(true);
					window.acquireAdmin();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SwingClient2() {
		if (System.getenv("GSHOME") != null) {
			gsHome = System.getenv("GSHOME");
		}
	}

	private void acquireAdmin() {
		service.submit(new AcquireFileResourcesAction(this, logPanel,
				"ece-datagrid.jar", "ece-worker.jar"));
		service.submit(new AcquireAdminAction(this, logPanel, gsHome, group,
				locator));
		dataGridPanel.setProcessingUnitName("ece-datagrid");
		dataGridPanel.setAdminContainer(this);
		dataGridPanel.setResourceContainer(this);
		dataGridPanel.setLogger(logPanel);
		workerPanel.setProcessingUnitName("ece-worker");
		workerPanel.setAdminContainer(this);
		workerPanel.setResourceContainer(this);
		workerPanel.setLogger(logPanel);
		workerPanel.setStatefulName("ece-datagrid");
		new Timer().schedule(new StatefulPUWatcher(dataGridPanel), 500, 1000);
		new Timer().schedule(new StatelessPUWatcher(workerPanel), 500, 1000);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmElasticCalculationEngine = new JFrame();
		frmElasticCalculationEngine.setTitle("Elastic Calculation Engine");
		frmElasticCalculationEngine.setBounds(100, 100, 650, 450);
		frmElasticCalculationEngine
				.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();

		gridBagLayout.columnWidths = new int[] { 642, 0 };
		gridBagLayout.rowHeights = new int[] { 150, 300, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };

		frmElasticCalculationEngine.getContentPane().setLayout(gridBagLayout);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.anchor = GridBagConstraints.NORTH;
		gbc_tabbedPane.weighty = 1.0;
		gbc_tabbedPane.fill = GridBagConstraints.HORIZONTAL;
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		frmElasticCalculationEngine.getContentPane().add(tabbedPane,
				gbc_tabbedPane);

		JPanel panelDeployment = new JPanel();
		tabbedPane.addTab("Deployment", null, panelDeployment, null);
		GridBagLayout gbl_panelDeployment = new GridBagLayout();
		gbl_panelDeployment.columnWidths = new int[] { 637, 0 };
		gbl_panelDeployment.rowHeights = new int[] { 20, 20, 0 };
		gbl_panelDeployment.columnWeights = new double[] { 0.0, 1.0 };
		gbl_panelDeployment.rowWeights = new double[] { 0.0, 0.0,
				Double.MIN_VALUE };
		panelDeployment.setLayout(gbl_panelDeployment);

		dataGridPanel = new StatefulPUPanel();
		GridBagConstraints gbc_dataGridPanel = new GridBagConstraints();
		gbc_dataGridPanel.fill = GridBagConstraints.BOTH;
		gbc_dataGridPanel.insets = new Insets(0, 0, 5, 0);
		gbc_dataGridPanel.gridx = 0;
		gbc_dataGridPanel.gridy = 0;
		panelDeployment.add(dataGridPanel, gbc_dataGridPanel);

		workerPanel = new StatelessPUPanel();
		GridBagConstraints gbc_workerPanel = new GridBagConstraints();
		gbc_workerPanel.fill = GridBagConstraints.BOTH;
		gbc_workerPanel.gridx = 0;
		gbc_workerPanel.gridy = 1;
		panelDeployment.add(workerPanel, gbc_workerPanel);

		JPanel panelExecution = new JPanel();
		tabbedPane.addTab("Execution", null, panelExecution, null);
		logPanel = new LogPanel();

		GridBagConstraints gbc_logPanel = new GridBagConstraints();
		gbc_logPanel.weighty = 3.0;
		gbc_logPanel.fill = GridBagConstraints.BOTH;
		gbc_logPanel.gridx = 0;
		gbc_logPanel.gridy = 1;
		frmElasticCalculationEngine.getContentPane()
				.add(logPanel, gbc_logPanel);

	}

	@Override
	public Admin getAdmin() {
		return admin;
	}

	@Override
	public void setAdmin(Admin admin) {
		this.admin = admin;
	}

	@Override
	public GridServiceAgent getGSA() {
		return GSA;
	}

	@Override
	public void setGSA(GridServiceAgent gridServiceAgent) {
		this.GSA = gridServiceAgent;
	}

	@Override
	public Map<String, File> getResources() {
		return resources;
	}

	@Override
	public void setResource(String key, File value) {
		resources.put(key, value);
	}
}
