package org.openspaces.ece.client.swing;

import java.io.*;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.ece.client.ClientLogger;

public class AcquireAdminAction implements Runnable {
	ContainsAdmin adminContainer;
	String gsHome;
	ClientLogger logger;
	String group;
	String locator;

	public AcquireAdminAction(ContainsAdmin adminContainer,
			ClientLogger logger, String gsHome, String group, String locator) {
		this.adminContainer = adminContainer;
		this.gsHome = gsHome;
		this.logger = logger;
		this.group = group;
		this.locator = locator;
	}

	@Override
	public void run() {
		File homeDir = new File(gsHome);
		File binDir = new File(homeDir, "bin");
		System.out.println(binDir);
		Admin admin;
		GridServiceAgent gsa;
		int retries = 10;
		adminContainer.setAdmin(admin = new AdminFactory().addGroup(group)
				.addLocator(locator).createAdmin());

		adminContainer.setGSA(gsa = admin.getGridServiceAgents()
				.waitForAtLeastOne(5, TimeUnit.SECONDS));
		final Process gsaProcess[] = new Process[1];

		if (gsa == null) {
			ProcessBuilder pb = new ProcessBuilder().directory(binDir);
			String command[];
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				command = new String[] { "cmd.exe", "/c", "gs-agent.bat" };
			} else {
				command = new String[] { "bash", "-c", "gs-agent.sh" };
			}
			// System.out.println(new File(binDir, command).exists());
			try {
				logger.log("Trying to start GSA, output is on stdout");
				gsaProcess[0] = pb
						.command(command[0], command[1], command[2],
								"global.gsa.esm", "1", "global.gsa.gsm", "2",
								"gsa.global.lus", "2", "gsa.gsc", "0")
						.redirectErrorStream(true).start();
				Thread loggerThread = new Thread() {
					public void run() {
						try {
							String line;
							BufferedReader r = new BufferedReader(
									new InputStreamReader(
											gsaProcess[0].getInputStream()));
							while ((line = r.readLine()) != null) {
								System.out.println(line);
							}
						} catch (IOException e) {
						}
					}
				};
				loggerThread.start();
			} catch (IOException e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				logger.log(sw.toString());
			}

			while (retries > 0 && gsa == null) {
				adminContainer.setGSA(gsa = admin.getGridServiceAgents()
						.waitForAtLeastOne(5, TimeUnit.SECONDS));
				logger.log(
						"Tries remaining to acquire GSA reference: %d (reference=%s)",
						retries, gsa);
				retries--;
			}

			if (gsa != null) {
				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						adminContainer.getGSA().shutdown();
					}
				});
			} else {
				gsaProcess[0].destroy();
			}
		}
		logger.log("Admin: %s%nGSA: %s", admin, gsa);
	}
}
