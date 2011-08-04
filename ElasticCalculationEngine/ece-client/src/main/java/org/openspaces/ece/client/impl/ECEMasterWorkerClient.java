package org.openspaces.ece.client.impl;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.calcengine.common.CalculateNPVUtil;
import org.openspaces.calcengine.masterworker.Request;
import org.openspaces.calcengine.masterworker.Result;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ECEMasterWorkerClient extends AbstractECEClient {
    int workersCount = 8;
    ProcessingUnit workerPU = null;
    double rates[] = {2, 3, 4, 5, 6, 7, 8};
    Logger logger = Logger.getLogger(this.getClass().getName());
    DecimalFormat formatter = new DecimalFormat("0.0");

    @Autowired
    PlatformTransactionManager ptm = null;
    @Autowired
    GigaSpace space;

    public ECEMasterWorkerClient() {
        Admin admin = new AdminFactory().addGroup("Gigaspaces-XAPPremium-8.0.3-rc").addLocator("127.0.0.1").createAdmin();
        System.out.println(Arrays.toString(admin.getVirtualMachines().getVirtualMachines()));
        workersCount = 0;
        System.out.println(admin.getProcessingUnits().getNames());
        workerPU = admin.getProcessingUnits().waitFor("ece-worker", 5, TimeUnit.SECONDS);
        if (workerPU != null) {
            System.out.println(workerPU);
            workerPU.addLifecycleListener(this);
            workersCount = workerPU.getNumberOfInstances();
        } else {
            System.out.println("No workers found; is this intentional? Master/Worker exiting.");
            admin.close();
        }
        valid = true;
    }

    public ECEMasterWorkerClient(int maxTrades) {
        this();
        setMaxTrades(maxTrades);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public ECEMasterWorkerClient(int maxTrades, int maxIterations) {
        this(maxTrades);
        setMaxIterations(maxIterations);
    }

    @Override
    public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        workersCount = workerPU.getNumberOfInstances();
    }

    @Override
    public void processingUnitInstanceRemoved(ProcessingUnitInstance processingUnitInstance) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        workersCount = workerPU.getNumberOfInstances();
    }

    @Override
    public void issueTrades() {
        Integer[] ids = new Integer[getMaxTrades()];
        for (int i = 0; i < getMaxTrades(); i++) {
            ids[i] = i;
        }

        for (double rate : rates) {
            for (int i = 0; i < getMaxIterations(); i++) {
                // Mapping IDs to worker
                HashMap<Integer, HashSet<Integer>> partitionIDSDistro = CalculateNPVUtil.splitIDs(ids, workersCount);
                long startTime = System.currentTimeMillis();
                logger.info("--> Executing Job " + i + " with " + workersCount + " workers");
                if (execute(i, partitionIDSDistro, rate)) {
                    reduce(i, partitionIDSDistro.size());
                }
                long endTime = System.currentTimeMillis();
                logger.info("--> Time to Execute Job " + i + " - " + (endTime - startTime) + "ms\n");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void reduce(int jobId, int totalResultsExpected) {
        int count = 0;
        int retryCount = 0;
        Result resultTemplate = new Result();
        resultTemplate.setJobID(jobId);
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus status = ptm.getTransaction(definition);

        HashMap<String, Double> aggregatedNPVCalc = new HashMap<String, Double>();
        while (true) {
            // getting results from all partitions
            Result results[] = space.takeMultiple(resultTemplate, Integer.MAX_VALUE);
            // Are there any results?
            if (results.length > 0) {
                count = count + results.length;
                // aggregate the results into books
                for (int i = 0; i < results.length; i++) {
                    HashMap<String, Double> incPositions = results[i].getResultData();
                    CalculateNPVUtil.subreducer(aggregatedNPVCalc, incPositions);
                }
            }
            // Do we have all the results?
            if (count == totalResultsExpected) {
                logger.info("--> Done executing Job " + jobId);
                break;
            }
            try {
                Thread.sleep(10);
                retryCount++;
                if (retryCount == 1000) {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            ptm.commit(status);
            for (String key : aggregatedNPVCalc.keySet()) {
                logger.info("Book = " + key + ", NPV = " + formatter.format(aggregatedNPVCalc.get(key)));
            }
        } catch (Exception e) {
            if (!status.isCompleted())
                ptm.rollback(status);
        }
    }

    public boolean execute(int jobId, HashMap<Integer, HashSet<Integer>> partitionIDSDistro, double rate) {
        TransactionStatus status = null;
        try {
            Request requests[] = new Request[partitionIDSDistro.size()];

            Iterator<Integer> iterator = partitionIDSDistro.keySet().iterator();
            int i = 0;
            while (iterator.hasNext()) {
                int key = iterator.next();
                HashSet<Integer> ids = partitionIDSDistro.get(key);
                requests[i] = new Request();
                requests[i].setJobID(jobId);
                requests[i].setTaskID(jobId + "_" + i);
                requests[i].setRouting(i % workersCount);
                requests[i].setRate(rate);
                Integer[] ids_ = new Integer[ids.size()];
                ids.toArray(ids_);
                requests[i].setTradeIds(ids_);
                i++;
            }
            DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
            status = ptm.getTransaction(definition);
            space.writeMultiple(requests);
            ptm.commit(status);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (!status.isCompleted())
                ptm.rollback(status);
        }
        return false;
    }
}
