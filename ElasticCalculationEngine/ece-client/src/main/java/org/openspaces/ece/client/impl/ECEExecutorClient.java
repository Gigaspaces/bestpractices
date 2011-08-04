package org.openspaces.ece.client.impl;

import com.gigaspaces.async.AsyncFuture;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.calcengine.common.CalculateNPVUtil;
import org.openspaces.core.ExecutorBuilder;
import org.openspaces.ece.client.executors.AnalysisTask;
import org.openspaces.ece.client.executors.NPVResultsReducer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ECEExecutorClient extends AbstractECEClient {
    int workersCount = 8;
    ProcessingUnit workerPU = null;
    double rates[] = {2, 3, 4, 5, 6, 7, 8};

    public ECEExecutorClient() {
        Admin admin = new AdminFactory().createAdmin();
        workerPU = admin.getProcessingUnits().waitFor("worker", 5, TimeUnit.SECONDS);
        if (workerPU != null) {
            workerPU.addLifecycleListener(this);
            workersCount = workerPU.getNumberOfInstances();
        }
    }

    public ECEExecutorClient(int maxTrades) {
        this();
        setMaxTrades(maxTrades);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public ECEExecutorClient(int maxTrades, int maxIterations) {
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
        // Mapping IDs to partition
        HashMap<Integer, HashSet<Integer>> partitionIDSDistro = CalculateNPVUtil.splitIDs(ids, workersCount);
        AnalysisTask analysisTasks[] = new AnalysisTask[workersCount];

        for (int c = 0; c < getMaxIterations(); c++) {
            Map<String, Double> positions;
            logger.info("Calculating Net present value for " + getMaxTrades() + " Trades ...");
            ExecutorBuilder<HashMap<String, Double>, HashMap<String, Double>> executorBuilder =
                    space.executorBuilder(new NPVResultsReducer());

            // Creating the Tasks. Each partition getting a Task with the exact Trade IDs to calculate
            for (double rate : rates) {
                long startTime = System.currentTimeMillis();
                for (int i = 0; i < workersCount; i++) {
                    Integer partIDs[] = new Integer[partitionIDSDistro.get(i).size()];
                    partitionIDSDistro.get(i).toArray(partIDs);
                    analysisTasks[i] = new AnalysisTask(partIDs, i, rate);
                    executorBuilder.add(analysisTasks[i]);
                }

                AsyncFuture<HashMap<String, Double>> future = executorBuilder.execute();

                if (future != null) {
                    try {
                        positions = future.get();
                        long endTime = System.currentTimeMillis();
                        logger.info("\nTime to calculate Net present value for "
                                + getMaxTrades() + " Trades using " + rate + " % rate:" + (endTime - startTime) + " ms");
                        for (String key : positions.keySet()) {
                            logger.info("Book = " + key + ", NPV = " + formatter.format(positions.get(key)));
                        }
                        Thread.sleep(1000);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }
}
