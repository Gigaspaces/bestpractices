package org.openspaces.ece.client.swing;

import java.io.File;

public class AcquireFileResourcesAction implements Runnable {
    LogPanel logPanel;
    ContainsResources container;
    String[] names;
    File home;

    public AcquireFileResourcesAction(ContainsResources container,
                                      LogPanel logPanel, String... names) {
        this.container = container;
        this.logPanel = logPanel;
        this.names = names;
        home = new File(System.getProperty("user.dir"));
        if (!home.getName().equals("ElasticCalculationEngine")) {
            home = new File(home, "..");
        }
    }

    @Override
    public void run() {
        for (String name : names) {
            String directory = name.substring(0, name.indexOf("."));
            logPanel.log("basename is %s", directory);
            File project = new File(home, directory);
            File target = new File(project, "target");
            File artifact = new File(target, name);
            if (artifact.exists()) {
                logPanel.log("found " + name);
                container.setResource(name, artifact);
            } else {
                logPanel.log("Could not find " + artifact);
            }
        }
    }
}
