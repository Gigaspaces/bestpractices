package com.gigaspaces.adminutils;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.admin.gsa.GridServiceManagerOptions;
import org.openspaces.admin.gsa.LookupServiceOptions;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.lus.LookupServices;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.vm.VirtualMachineDetails;
import org.openspaces.admin.vm.VirtualMachineStatistics;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.gigaspaces.adminutils.common.AdminUtil;

/*
 * Grid Manager can be used to manage GigaSpaces Grid.
 * It supports following commands,
 * 	start 		- Starts the grid and deploy the applications 
 *  startOne 	- Starts the grid components on the machine address passed as argument 
 *  		   	  and invokes the deploy for all the applications if this machine is a deployer machine
 *  stop 		- Undeploy the applications and Stop the grid
 *  stopOne 	- Stops the grid components on the machine address passed as argument
 *  restart 	- Undeploy, stop all the grid, start all the grid, deploy the applications  
 *  status 		- Show current status of the grid
 *  
 *  It is based on following assumptions,
 *   - Agents are already started on each machine that is part of the grid
 *   - Lookup servers should be running before running GridManager
 *   	(Tip: Start agents with lookup server, if using multicast start agents with gsa.global.lus option)
 *   - Only one agent is expected to be running on each machine
 *  
 */
public class GridManager {

	private static Logger logger = Logger.getLogger(GridManager.class
			.getSimpleName());

	@Parameter(names = { "--locators", "-l" }, description = "LOOKUPLOCATORS")
	public String locators = "localhost";

	@Parameter(names = { "--groups", "-g" }, description = "LOOKUPGROUPS")
	public String groups;

	@Parameter(names = { "--config" }, description = "Complete path to config file")
	public String configFileLocation = "java.properties";

	@Parameter(names = { "--command" }, description = "Command to execute. Supported commands, "
			+ "start (All machines), "
			+ "startOne (one machine only), "
			+ "stop (All machines), "
			+ "stopOne (one machine only), "
			+ "restart (All machines), " + "status")
	public String command;

	@Parameter(names = { "--hostAddress", "-h" }, description = "Address of the machine for startOne and stopOne commands")
	public String agentHostAddress;

	@Parameter(names = { "--user", "-u" }, description = "User Name with Grid Manage Authority (if security is enabled)")
	public String user;

	@Parameter(names = { "--password", "p" }, description = "Password (if security is enabled)")
	public String password;

	@Parameter(names = { "--validateGrid", "-v" }, description = "Set this flag if Manager should validate current state before starting the components")
	public Boolean validateGrid = false;

	private Admin gsAdmin;
	private String[] machines;
	private Properties properties;
	private Map<String, Map<String, Integer>> machineComponents;
	private Map<String, Map<String, String>> machineComponentJvmargs;
	private Map<String, String> machineCommonJvmargs;

	private final int DEFAULT_TIMEOUT = 60; // 1 minute - 60 seconds
	private final int DEPLOY_TIMEOUT = 5 * 60 * 1000; // Wait for 5 minutes to
														// deploy

	private final String START_COMMAND = "start";
	private final String START_ONE_COMMAND = "startOne";
	private final String RESTART_COMMAND = "restart";
	private final String STOP_COMMAND = "stop";
	private final String STOP_ONE_COMMAND = "stopOne";
	private final String STATUS_COMMAND = "status";

	private final String MACHINES_KEY = "machines";
	private final String DEPLOYER_MACHINES_KEY = "deployermachines";
	private final String LUS_KEY = "lus";
	private final String GSM_KEY = "gsm";
	private final String GSC_KEY = "gsc";
	private final String COMMON_JVM_ARGS_KEY = "commonjvmargs";
	private final String JVM_ARGS_KEY = "jvmargs";

	private final String PULIST_KEY = "pulist";
	private final String DEPLOYMENT_KEY = "deployment";
	private final String DEPLOYMENT_OPTIONS_KEY = "deployment.options";
	private final String DEPLOY_OPTIONS_SEPERATOR = " -";

	private NumberFormat memoryFormatter = new DecimalFormat("#0.00");
	private NumberFormat percentFormatter = new DecimalFormat("#0.00");

	/*
	 * Starts the GridManager and runs the command that is passed
	 */
	public static void main(String[] args) throws Exception {

		GridManager gm = new GridManager();

		new JCommander(gm, args);

		gm.init();
		gm.processCommand();

		// Completed everything. Exit the program
		System.exit(0);
	}

	/*
	 * Creates Admin Instance
	 */
	private void init() {

		AdminFactory af = new AdminFactory();

		if (locators != null)
			af.addLocator(locators);

		if (groups != null)
			af.addGroups(groups);

		if (locators == null && groups == null) {
			logger.info("LOOKUPLOCATORS or LOOKUPGROUPS should be passed to use this tool.");
			System.exit(-1);
		}

		if ((command.equals(START_ONE_COMMAND) || command
				.equals(STOP_ONE_COMMAND)) && agentHostAddress == null) {
			logger.info("When using startOne or stopOne command, "
					+ "you have to pass the host address using \"--hostAddress\" or \"-h\" parameter.");
			System.exit(-1);
		}

		if (user != null) {
			af.userDetails(user, password);
		}

		gsAdmin = af.createAdmin();
		gsAdmin.setSpaceMonitorInterval(1, TimeUnit.SECONDS);

		if (gsAdmin == null) {
			logger.info("Cannot create GigaSpaces Admin - Exiting GridManager");
			System.exit(1);
		}

		displayConfig();
		logger.info("Admin created successfully. Sleeping for discovery");
		logger.info("Discovering Grid Components");

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Read the configuration
		readConfiguration();

	}

	/*
	 * Read the configuration file and populate configuration parameters
	 */
	private void readConfiguration() {

		if (configFileLocation != null) {
			properties = new Properties();
			try {
				properties.load(new FileInputStream(configFileLocation));
			} catch (Exception e) {
				logger.info("Error Reading the configuration file");
				e.printStackTrace();
			}
		}
	}

	/*
	 * Display the configuration that is being used
	 */
	private void displayConfig() {

		logger.info("Running the GridManager client with following parameters - ");
		logger.info("locators [" + locators + "]");
		logger.info("groups [" + groups + "]");
		logger.info("config [" + configFileLocation + "]");
		logger.info("hostAddress [" + agentHostAddress + "]");
		logger.info("validateGrid [" + validateGrid + "]");
		logger.info("command [" + command + "]");

	}

	/*
	 * Processes the Command
	 */
	private void processCommand() {

		if (command != null) {
			// Start the Grid and Deploy the processing units
			if (command.equals(START_COMMAND)
					|| command.equals(START_ONE_COMMAND)) {
				getMachineConfig();
				startGrid();
				deployPus();
			}
			// Undeploy the processing units and Stop the Grid
			if (command.equals(STOP_COMMAND)) {
				undeployPus();
				stopGrid();
			}
			// Stop the Grid components on this machine
			if (command.equals(STOP_ONE_COMMAND)) {
				stopGrid();
			}
			// Undeploy the processing units and Stop the Grid, Start the Grid
			// and Deploy the processing units
			if (command.equals(RESTART_COMMAND)) {
				getMachineConfig();
				undeployPus();
				stopGrid();
				startGrid();
				deployPus();
			}
			// Display current Status
			if (command.equals(STATUS_COMMAND)) {
				getGridStatus();
			}
		}

	}

	/*
	 * Process machine configuration
	 */
	private void getMachineConfig() {

		String machinesStr = properties.getProperty(MACHINES_KEY);
		machines = machinesStr.split(",");

		if (machines != null && machines.length != 0) {

			// Get the ip addresses of machines
			for (int i = 0; i < machines.length; i++) {
				String machine = machines[i];
				try {
					String ip = AdminUtil.lookup(machine);
					machines[i] = ip;
				} catch (Throwable e) {
					logger.info("Error looking up hostname " + machine);
					e.printStackTrace();
					System.exit(-1);
				}

			}

			machineComponents = new HashMap<String, Map<String, Integer>>();
			machineComponentJvmargs = new HashMap<String, Map<String, String>>();
			machineCommonJvmargs = new HashMap<String, String>();

			// Get Machine components for each Machine
			for (String machine : machines) {

				String machineIp = null;

				try {
					machineIp = AdminUtil.lookup(machine);
				} catch (Throwable e) {
					logger.info("Error looking up hostname " + machine);
					e.printStackTrace();
					System.exit(-1);
				}
				
				Map<String, Integer> componentMap = new HashMap<String, Integer>();
				Map<String, String> jvmargsMap = new HashMap<String, String>();

				try {

					String commonJvmargs = properties.getProperty(machine + "."
							+ COMMON_JVM_ARGS_KEY);
					
					if (commonJvmargs != null) {
						machineCommonJvmargs.put(machineIp, commonJvmargs);
					}

					int lus = Integer.parseInt(properties.getProperty(machine
							+ "." + LUS_KEY, "0"));
					componentMap.put(LUS_KEY, lus);

					String lusArgs = properties.getProperty(machine + "."
							+ LUS_KEY + "." + JVM_ARGS_KEY);
					jvmargsMap.put(LUS_KEY, lusArgs);

					int gsm = Integer.parseInt(properties.getProperty(machine
							+ "." + GSM_KEY, "0"));
					componentMap.put(GSM_KEY, gsm);

					String gsmArgs = properties.getProperty(machine + "."
							+ GSM_KEY + "." + JVM_ARGS_KEY);
					jvmargsMap.put(GSM_KEY, gsmArgs);

					int gsc = Integer.parseInt(properties.getProperty(machine
							+ "." + GSC_KEY, "0"));
					componentMap.put(GSC_KEY, gsc);

					String gscArgs = properties.getProperty(machine + "."
							+ GSC_KEY + "." + JVM_ARGS_KEY);
					jvmargsMap.put(GSC_KEY, gscArgs);

					logger.info("Machine - " + machine
							+ " configuration map - " + componentMap);

				} catch (Exception e) {
					logger.info("Error Processing config parameters for Machine "
							+ machine);
					e.printStackTrace();
					System.exit(-1);
				}

				machineComponents.put(machineIp, componentMap);
				machineComponentJvmargs.put(machineIp, jvmargsMap);

			}
		}

	}

	/*
	 * Start Grid assumes agents are running on the machines. Discovers the
	 * agents and uses the appropriate config for each machine and starts the
	 * grid components
	 */
	private void startGrid() {

		if (machineComponents != null && machineComponents.size() != 0) {
			GridServiceAgents agents = gsAdmin.getGridServiceAgents();
			agents.waitForAtLeastOne(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

			if (!agents.isEmpty()) {

				for (GridServiceAgent agent : agents) {
					String hostAddress = agent.getMachine().getHostAddress();

					// Start Agent components only if it is start or startOne
					// for the correct machine
					if ((command.equals(START_COMMAND) || (command
							.equals(START_ONE_COMMAND) && hostAddress
							.equals(agentHostAddress)))) {

						startAgentComponents(agent);
					} else {
						logger.fine("Ignored agent, Uid - " + agent.getUid()
								+ ", on Host - " + hostAddress);
					}
				}
			} else {
				logger.info("No Agents found. Nothing to start");
			}
		} else {
			logger.info("No Machine Components configuration found. Nothing to start");
		}

	}

	/*
	 * Start the components for the agent
	 */
	private void startAgentComponents(GridServiceAgent agent) {

		String hostAddress = agent.getMachine().getHostAddress();

		logger.info("Starting components for Agent " + agent.getUid() + " on "
				+ hostAddress);

		// Get config for the machine
		Map<String, Integer> componentMap = machineComponents.get(hostAddress);
		Map<String, String> jvmargsMap = machineComponentJvmargs
				.get(hostAddress);

		Map<String, Integer> currentStatus = new HashMap<String, Integer>();

		if (validateGrid) {
			currentStatus = getAgentStatus(agent);
		}

		// Start Lookup Server(s)
		if (componentMap != null) {

			String commonJvmargsStr = machineCommonJvmargs.get(hostAddress);
			String commonJvmargs[] = null;
			if (commonJvmargsStr != null) {
				commonJvmargs = commonJvmargsStr.split(" ");
			}

			if (componentMap.containsKey(LUS_KEY)) {

				int currentLusCount = 0;
				if (currentStatus.get(LUS_KEY) != null) {
					currentLusCount = currentStatus.get(LUS_KEY);
				}
				if ((componentMap.get(LUS_KEY) - currentLusCount) > 0) {

					LookupServiceOptions lusOpts = new LookupServiceOptions();
					for (String commonJvmarg : commonJvmargs) {
						lusOpts.vmInputArgument(commonJvmarg);
					}

					String lusOptionsStr[] = jvmargsMap.get(LUS_KEY).split(" ");
					for (String lusOptionStr : lusOptionsStr) {
						lusOpts.vmInputArgument(lusOptionStr);
					}

					for (int i = 0; i < (componentMap.get(LUS_KEY) - currentLusCount); i++) {

						logger.info("Starting LUS " + (i + 1) + " on "
								+ hostAddress);
						agent.startGridService(lusOpts);
					}
				}

			}

			// Start GSM's
			if (componentMap.containsKey(GSM_KEY)) {

				int currentGsmCount = 0;
				if (currentStatus.get(GSM_KEY) != null) {
					currentGsmCount = currentStatus.get(GSM_KEY);
				}

				if ((componentMap.get(GSM_KEY) - currentGsmCount) > 0) {

					GridServiceManagerOptions gsmOpts = new GridServiceManagerOptions();
					for (String commonJvmarg : commonJvmargs) {
						gsmOpts.vmInputArgument(commonJvmarg);
					}

					String gsmOptionsStr[] = jvmargsMap.get(GSM_KEY).split(" ");
					for (String gsmOptionStr : gsmOptionsStr) {
						gsmOpts.vmInputArgument(gsmOptionStr);
					}

					for (int i = 0; i < (componentMap.get(GSM_KEY) - currentGsmCount); i++) {

						logger.info("Starting GSM " + (i + 1) + " on "
								+ hostAddress);
						agent.startGridServiceAndWait(gsmOpts, DEFAULT_TIMEOUT,
								TimeUnit.SECONDS);
					}
				}

			}

			// Start GSC's
			if (componentMap.containsKey(GSC_KEY)) {

				int currentGscCount = 0;
				if (currentStatus.get(GSC_KEY) != null) {
					currentGscCount = currentStatus.get(GSC_KEY);
				}

				if ((componentMap.get(GSC_KEY) - currentGscCount) > 0) {

					GridServiceContainerOptions gscOpts = new GridServiceContainerOptions();
					for (String commonJvmarg : commonJvmargs) {
						gscOpts.vmInputArgument(commonJvmarg);
					}

					String gscOptionsStr[] = jvmargsMap.get(GSC_KEY).split(" ");
					for (String gscOptionStr : gscOptionsStr) {
						gscOpts.vmInputArgument(gscOptionStr);
					}

					for (int i = 0; i < (componentMap.get(GSC_KEY) - currentGscCount); i++) {
						logger.info("Starting GSC " + (i + 1) + " on "
								+ hostAddress);

						agent.startGridServiceAndWait(gscOpts, DEFAULT_TIMEOUT,
								TimeUnit.SECONDS);
					}
				}

			}
		}

	}

	/*
	 * Get components attached to the agent
	 */
	private Map<String, Integer> getAgentStatus(GridServiceAgent agent) {

		Map<String, Integer> currentState = new HashMap<String, Integer>();

		String hostAddress = agent.getMachine().getHostAddress();

		logger.info("Getting Agent Status for - " + agent.getUid()
				+ " on host - " + hostAddress);

		LookupServices luss = agent.getMachine().getLookupServices();

		currentState.put(LUS_KEY, luss.getSize());

		GridServiceManagers gsms = agent.getMachine().getGridServiceManagers();

		currentState.put(GSM_KEY, gsms.getSize());

		GridServiceContainers gscs = agent.getMachine()
				.getGridServiceContainers();

		currentState.put(GSC_KEY, gscs.getSize());

		return currentState;

	}

	/*
	 * Deploy the list of processing units
	 */
	private void deployPus() {

		// PUs to be deployed
		List<String> pusToBeDeployed = getPusToBeDeployed();

		GridServiceManagers gsms = gsAdmin.getGridServiceManagers();

		GridServiceManager gsm = gsms.waitForAtLeastOne(DEFAULT_TIMEOUT,
				TimeUnit.SECONDS);

		if (gsm == null) {
			logger.info("Cannot get a handle on GSM. Cannot continue deploy");
			System.exit(-1);
		}

		ProcessingUnitDeployment puDeployConfig = null;
		ProcessingUnit pu = null;

		for (String puName : pusToBeDeployed) {
			long stTime = System.currentTimeMillis(), enTime = 0;
			boolean deployOK = false;

			// Set Deployment Options
			puDeployConfig = createDeployment(puName);

			logger.info("Invoking Deploy command for PU - " + puName);

			// Invoke deploy
			try {
				pu = gsm.deploy(puDeployConfig, DEFAULT_TIMEOUT,
						TimeUnit.SECONDS);

				// Wait for deploy to complete
				while (true) {
					pu = gsAdmin.getProcessingUnits().waitFor(puName, 10,
							TimeUnit.SECONDS);
					if (pu == null) {
						logger.info("Can't acces PU. Abort Deploy process");
						System.exit(-1);
					}
					DeploymentStatus deployStatus = pu.getStatus();
					if (deployStatus == null) {
						break;
					}
					if (deployStatus.equals(DeploymentStatus.INTACT)) {
						deployOK = true;
						enTime = System.currentTimeMillis();
						break;
					}
					if (System.currentTimeMillis() > (stTime + DEPLOY_TIMEOUT)) {
						break;
					}
					try {
						logger.info("Waiting for PU - " + puName
								+ " to deploy...");
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (deployOK) {
					logger.info("PU - " + puName
							+ " deployed sucessfuly. Time to deploy: "
							+ (enTime - stTime) / 1000 + " seconds");
				} else {
					logger.info("PU - " + puName
							+ " cannot be deployed sucessfuly.");
					System.exit(-1);
				}
			} catch (AdminException e) {
				logger.info("PU - " + puName
						+ " cannot be deployed sucessfuly.");
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	private List<String> getPusToBeDeployed() {

		List<String> pusToBeDeployed = new ArrayList<String>();
		
		String deployerMachinesStr = properties
				.getProperty(DEPLOYER_MACHINES_KEY);
		String[] deployerMachinesList = deployerMachinesStr.split(",");

		// Check if deploy should be run or not
		if (command.equals(START_ONE_COMMAND)) {
			boolean deployFlag = false;
			for (String deployerMachine : deployerMachinesList) {
				if (deployerMachine.equals(agentHostAddress)) {
					deployFlag = true;
					break;
				}
			}
			if (!deployFlag) {
				return null;
			}
		}

		// Get current deployed PUs
		ProcessingUnits pus = gsAdmin.getProcessingUnits();
		Set<String> currentPuNames = null;

		if (!pus.isEmpty()) {
			currentPuNames = pus.getNames().keySet();
		}

		// Get Configured PU list
		String puNameListStr = properties.getProperty(PULIST_KEY);
		String[] puNameList = puNameListStr.split(",");

		if (currentPuNames != null && currentPuNames.size() > 0) {
			for (String puName : puNameList) {
				if (!currentPuNames.contains(puName)) {
					pusToBeDeployed.add(puName);
				}
			}
		} else {
			pusToBeDeployed = Arrays.asList(puNameList);
		}
		return pusToBeDeployed;
	}

	/*
	 * Get Deployment option for the processing unit
	 */
	private ProcessingUnitDeployment createDeployment(String puName) {

		ProcessingUnitDeployment puDeployConfig = null;

		// Get the processing deployment package location
		String puDeployment = properties.getProperty(puName + "."
				+ DEPLOYMENT_KEY);

		// Only zip/jar/war types are expected
		if (puDeployment != null) {
			if (puDeployment.toLowerCase().endsWith(".zip")
					|| puDeployment.toLowerCase().endsWith(".jar")
					|| puDeployment.toLowerCase().endsWith(".war")) {

				puDeployConfig = new ProcessingUnitDeployment(new File(
						puDeployment));
				puDeployConfig.name(puName);
				setDeploymentOptions(puName, puDeployConfig);

			} else {
				logger.info("file does not ends with zip/jar/war. Deploy Exit!");
				System.exit(-1);
			}
		} else {
			logger.info("Cannot find the deployment location property for Processing Unit "
					+ puName);
			System.exit(-1);
		}
		return puDeployConfig;
	}

	/*
	 * Set Deployment Options
	 */
	private void setDeploymentOptions(String puName, ProcessingUnitDeployment puDeployConfig) {

		// Get deployment options for the PU
		String deployOptions = properties.getProperty(puName + "." + DEPLOYMENT_OPTIONS_KEY);

		Properties deployProps = new Properties();

		if (deployOptions != null && !deployOptions.trim().equals("")) {

			// Prefix with " ", for being able to pick the first property
			deployOptions = " " + deployOptions;

			String deplOptions[] = deployOptions
					.split(DEPLOY_OPTIONS_SEPERATOR);
			for (int i = 0; i < deplOptions.length; i++) {
				String prop[] = deplOptions[i].split(" ");
				String option = prop[0];
				if ((option != null) && (option.length() > 0)) {
					String value = prop[1];
					if (prop.length > 2) {
						for (int j = 2; j < prop.length; j++) {
							value = value + " " + prop[j];
						}
					}
					deployProps.put(option.toLowerCase(), value);
				}
			}

		}

		// Set SLA location
		if (deployProps.containsKey("sla")) {
			puDeployConfig.slaLocation(deployProps.getProperty("sla"));
		}

		// Get Cluster parameters
		if (deployProps.containsKey("cluster")) {
			String clusterConfig = deployProps.getProperty("cluster");
			StringTokenizer stclusterConfig = new StringTokenizer(
					clusterConfig, " ");
			while (stclusterConfig.hasMoreTokens()) {
				String token = stclusterConfig.nextToken();
				String clusterOptions[] = token.split("=");
				if (clusterOptions[0].equals("schema")) {
					puDeployConfig.clusterSchema(clusterOptions[1]);
				}
				if (clusterOptions[0].equals("total_members")) {
					String clusterMembers[] = clusterOptions[1].split(",");
					puDeployConfig.numberOfInstances(Integer.valueOf(
							clusterMembers[0]).intValue());
					if (clusterMembers.length == 2)
						puDeployConfig.numberOfBackups(Integer.valueOf(
								clusterMembers[1]).intValue());
				}
			}
		}

		boolean secured = false;
		// Get secured option
		if (deployProps.containsKey("secured")) {
			secured = Boolean.valueOf(deployProps.getProperty("secured"))
					.booleanValue();

			puDeployConfig.secured(secured);
		}

		// Set user id/password if it is a secured space
		if (secured) {
			if (deployProps.containsKey("user")) {
				String user = deployProps.getProperty("user");
				String pass = deployProps.getProperty("password");

				puDeployConfig.userDetails(user, pass);
			}
		}

	}

	/*
	 * Undeploy the list of processing units
	 */
	private void undeployPus() {

		// PUs to be undeployed
		String[] puNameList = null;
		
		Set<String> filteredPuList = new HashSet<String>();

		String puListStr = properties.getProperty(PULIST_KEY);
		puNameList = puListStr.split(",");

		// Get current deployed PUs
		ProcessingUnits pus = gsAdmin.getProcessingUnits();
		Set<String> currentPuNames = null;

		if (!pus.isEmpty()) {
			currentPuNames = pus.getNames().keySet();
		}


		if (currentPuNames != null && currentPuNames.size() > 0) {
			for (String puName : puNameList) {
				if (currentPuNames.contains(puName)) {
					filteredPuList.add(puName);
				}
			}
		} else {
			filteredPuList = currentPuNames;
		}

		if (filteredPuList == null || filteredPuList.size() == 0) {
			logger.info("Nothing to undeploy");
			return;
		}

		puNameList = filteredPuList.toArray(new String[filteredPuList.size()]);

		GridServiceManagers gsms = gsAdmin.getGridServiceManagers();

		GridServiceManager gsm = gsms.waitForAtLeastOne(DEFAULT_TIMEOUT,
				TimeUnit.SECONDS);

		if (gsm == null) {
			logger.info("Cannot get a handle on GSM. Cannot continue deploy");
			System.exit(-1);
		}

		// Undeploy the processing units in the reverse order
		for (int i = puNameList.length - 1; i >= 0; i--) {
			long stTime = System.currentTimeMillis(), enTime = 0;
			boolean undeployOK = false;

			String puName = puNameList[i];

			try {
				logger.info("Undeploying " + puName);
				gsm.undeploy(puName);

				// wait for undeploy to complete
				while (true) {
					pus = gsAdmin.getProcessingUnits();
					if (!pus.getNames().containsKey(puName)) {
						undeployOK = true;
						enTime = System.currentTimeMillis();
						break;
					}

					if (System.currentTimeMillis() > (stTime + 60000)) {
						break;
					}

					try {
						logger.info("Waiting for PU - " + puName + " undeploy to complete...");
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (undeployOK) {
					logger.info("PU - " + puName
							+ " undeployed successfully in "
							+ (enTime - stTime) / 1000 + " seconds");
				}

			} catch (AdminException e) {
				e.printStackTrace();
				logger.info("Error Undeploying the PU - " + puName);
			}
		}

	}

	/*
	 * Discovers the agents and uses the appropriate config for each machine and
	 * starts the grid components
	 */
	private void stopGrid() {

		GridServiceAgents agents = gsAdmin.getGridServiceAgents();
		agents.waitForAtLeastOne(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

		if (!agents.isEmpty()) {
			for (GridServiceAgent agent : agents) {
				String hostAddress = agent.getMachine().getHostAddress();

				if ((command.equals(STOP_COMMAND) || (command
						.equals(STOP_ONE_COMMAND) && hostAddress
						.equals(agentHostAddress)))) {

					stopAgentComponents(agent);
				} else {
					logger.fine("Ignored agent, Uid - " + agent.getUid()
							+ ", on Host - " + hostAddress);
				}
			}
		} else {
			logger.info("No Agents found. Nothing to start");
		}

	}

	private void stopAgentComponents(GridServiceAgent agent) {

		String hostAddress = agent.getMachine().getHostAddress();

		logger.info("Stopping components for Agent " + agent.getUid() + " on "
				+ hostAddress);

		GridServiceContainers containers = agent.getMachine()
				.getGridServiceContainers();

		// Kill all GSC's
		for (GridServiceContainer container : containers) {
			logger.info("Stopping Grid Service Container, Agent Id - "
					+ container.getAgentId() + " Uid - " + container.getUid());

			container.kill();
		}

		GridServiceManagers managers = agent.getMachine()
				.getGridServiceManagers();

		// Kill all GSM's
		for (GridServiceManager manager : managers) {
			logger.info("Stopping Grid Service Manager " + manager.getUid());

			manager.kill();
		}

	}

	/*
	 * Prints the current status of the Grid, including GSM list, GSC List and
	 * processing units that are running
	 */
	private void getGridStatus() {

		GridServiceAgents agents = gsAdmin.getGridServiceAgents();
		agents.waitForAtLeastOne(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

		if (!agents.isEmpty()) {
			for (GridServiceAgent agent : agents) {

				getAgentComponents(agent);
			}
		} else {
			logger.info("No Agents found. Nothing to start");
		}

	}

	/*
	 * Get components attached to the agent
	 */
	private void getAgentComponents(GridServiceAgent agent) {

		String hostAddress = agent.getMachine().getHostAddress();

		logger.info("Printing component information for Agent Uid - "
				+ agent.getUid() + " on host - " + hostAddress);

		GridServiceManagers gsms = agent.getMachine().getGridServiceManagers();

		// Display all GSM's
		for (GridServiceManager gsm : gsms) {

			VirtualMachineDetails vmDetails = gsm.getVirtualMachine()
					.getDetails();
			VirtualMachineStatistics vmStats = gsm.getVirtualMachine()
					.getStatistics();

			logger.info("GSM, Uid - " + gsm.getUid() + ", Heap Usage - "
					+ percentFormatter.format(vmStats.getMemoryHeapUsedPerc())
					+ "% : "
					+ memoryFormatter.format(vmStats.getMemoryHeapUsedInMB())
					+ "MB, Max Heap "
					+ memoryFormatter.format(vmDetails.getMemoryHeapMaxInMB())
					+ "MB.");
		}

		GridServiceContainers gscs = agent.getMachine()
				.getGridServiceContainers();

		// Display all GSC's
		for (GridServiceContainer gsc : gscs) {
			VirtualMachineDetails vmDetails = gsc.getVirtualMachine()
					.getDetails();
			VirtualMachineStatistics vmStats = gsc.getVirtualMachine()
					.getStatistics();

			logger.info("GSC, Agent Id - " + gsc.getAgentId() + ", Uid - "
					+ gsc.getUid() + ", Heap Usage - "
					+ percentFormatter.format(vmStats.getMemoryHeapUsedPerc())
					+ "% : "
					+ memoryFormatter.format(vmStats.getMemoryHeapUsedInMB())
					+ "MB, Max Heap "
					+ memoryFormatter.format(vmDetails.getMemoryHeapMaxInMB())
					+ "MB.");

			for (ProcessingUnitInstance puInstance : gsc
					.getProcessingUnitInstances()) {
				logger.info("   Contains PU - " + puInstance.getName()
						+ ", Instance Id - " + puInstance.getInstanceId()
						+ ", Backup Id - " + puInstance.getBackupId());
			}
		}
	}

}
