package org.openspaces.ece.client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.ece.client.builders.ClientBuilder;
import org.openspaces.ece.client.builders.ClientLoggerBuilder;
import org.openspaces.ece.client.clients.NoWorkersAvailableException;

import java.util.concurrent.TimeUnit;

public class ConsoleClient implements ContainsAdmin {
    @Parameter(names = {"-type"}, description = "The type of the client to run (masterworker/executor)")
    public String type = "masterworker";
    @Parameter(names = "-iterations", description = "the number of iterations to run")
    public Integer maxIterations = 10;
    @Parameter(names = "-trades", description = "the number of trades to run")
    public Integer maxTrades = 100;
    @Parameter(names = {"-l", "--locator"})
    String locator = "127.0.0.1";
    @Parameter(names = {"-g", "--group"})
    String group = "Gigaspaces-XAPPremium-8.0.3-ga";
    Admin admin;
    GridServiceAgent gsa;

    public static void main(String... args) throws NoWorkersAvailableException {
        ConsoleClient client = new ConsoleClient();
        new JCommander(client, args);
        client.run();
    }

    private void run() throws NoWorkersAvailableException {
        System.out.println("Running " + type);
        admin = new AdminFactory().addGroup(group)
                .addLocator(locator).createAdmin();

        gsa = admin.getGridServiceAgents()
                .waitForAtLeastOne(5, TimeUnit.SECONDS);

        ECEClient eceClient = new ClientBuilder()
                .type(type)
                .group(group)
                .locator(locator)
                .trades(maxTrades)
                .iterations(maxIterations)
                .admin(this)
                .logger(new ClientLoggerBuilder().console().build())
                .spaceUrl("jini://*/*/ece-datagrid")
                .build();
        eceClient.init();
        if (eceClient.isValid()) {
            eceClient.issueTrades();
        }
        // we exit explicitly because I don't feel like tracing the resource lock
        System.exit(0);
    }

    ConsoleClient() {
    }

    @Override
    public Admin getAdmin() {
        return admin;
    }

    @Override
    public GridServiceAgent getGSA() {
        return gsa;
    }

    @Override
    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    @Override
    public void setGSA(GridServiceAgent gridServiceAgent) {
        this.gsa = gridServiceAgent;
    }
}
