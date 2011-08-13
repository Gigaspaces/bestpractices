package org.openspaces.ece.client.clients;

import com.j_spaces.core.IJSpace;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.core.transaction.manager.DistributedJiniTransactionManager;
import org.openspaces.core.transaction.manager.DistributedJiniTxManagerConfigurer;
import org.openspaces.ece.client.ClientLogger;
import org.openspaces.ece.client.ECEClient;
import org.openspaces.ece.client.builders.ClientLoggerBuilder;
import org.openspaces.ece.client.swing.ContainsAdmin;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.TimeUnit;

public abstract class AbstractECEClient implements ECEClient,
        ProcessingUnitInstanceLifecycleEventListener {
    ClientLogger logger = new ClientLoggerBuilder().console().build();
    private String spaceUrl;
    String locator;
    String group;
    ContainsAdmin adminContainer;
    ProcessingUnit worker;
    int workers = 2;
    boolean valid = true;
    PlatformTransactionManager ptm = null;
    GigaSpace space;
    int maxTrades = 10000;
    int maxIterations = 100;

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String getLocator() {
        return locator;
    }

    @Override
    public void setLocator(String locator) {
        this.locator = locator;
    }

    @Override
    public void setContainsAdmin(ContainsAdmin admin) {
        adminContainer = admin;
    }

    @Override
    public void init() throws NoWorkersAvailableException {
        UrlSpaceConfigurer configurer = new UrlSpaceConfigurer(spaceUrl);
        if (getGroup() != null) {
            configurer.lookupGroups(getGroup());
        }
        if (getLocator() != null) {
            configurer.lookupLocators(getLocator());
        }
        IJSpace ijspace = configurer.create();
        try {
            ptm = new DistributedJiniTxManagerConfigurer().transactionManager();
        } catch (Exception e) {
            throw new Error(e);
        }
        System.out.println("transaction manager: "+ptm);
        space = new GigaSpaceConfigurer(ijspace).transactionManager(ptm).create();
        worker = adminContainer.getAdmin().getProcessingUnits().waitFor("ece-worker", 5, TimeUnit.SECONDS);
        if (worker == null) {
            throw new NoWorkersAvailableException();
        }
        worker.addLifecycleListener(this);
        workers = worker.getNumberOfInstances();
    }

    @Override
    public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.log("New processing unit instance added. Was %d, is now %d", workers, worker.getNumberOfInstances());
        workers = worker.getNumberOfInstances();
    }

    @Override
    public void processingUnitInstanceRemoved(ProcessingUnitInstance processingUnitInstance) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.log("New processing unit instance removed. Was %d, is now %d", workers, worker.getNumberOfInstances());
        workers = worker.getNumberOfInstances();
    }

    @Override
    public void setSpaceUrl(String spaceUrl) {
        this.spaceUrl = spaceUrl;
    }

    @Override
    public void setClientLogger(ClientLogger logger) {
        this.logger = logger;
    }

    public PlatformTransactionManager getPtm() {
        return ptm;
    }

    public void setPtm(PlatformTransactionManager ptm) {
        this.ptm = ptm;
    }

    public GigaSpace getSpace() {
        return space;
    }

    public void setSpace(GigaSpace space) {
        this.space = space;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public int getMaxTrades() {
        return maxTrades;
    }

    @Override
    public void setMaxTrades(int maxTrades) {
        this.maxTrades = maxTrades;
    }

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }
}
