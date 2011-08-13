package org.openspaces.ece.client.clients;

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
    double rates[] = {2, 3, 4, 5, 6, 7, 8};

    public ECEExecutorClient() {
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
        workers = worker.getNumberOfInstances();
    }

    @Override
    public void processingUnitInstanceRemoved(ProcessingUnitInstance processingUnitInstance) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        workers= worker.getNumberOfInstances();
    }

    @Override
    public void issueTrades() {
        Integer[] ids = new Integer[getMaxTrades()];
        for (int i = 0; i < getMaxTrades(); i++) {
            ids[i] = i;
        }
        // Mapping IDs to partition
        HashMap<Integer, HashSet<Integer>> partitionIDSDistro = CalculateNPVUtil.splitIDs(ids, workers);
        AnalysisTask analysisTasks[] = new AnalysisTask[workers];

        for (int c = 0; c < getMaxIterations(); c++) {
            Map<String, Double> positions;
            logger.log("Calculating Net present value for %d Trades ...", getMaxTrades());
            ExecutorBuilder<HashMap<String, Double>, HashMap<String, Double>> executorBuilder =
                    space.executorBuilder(new NPVResultsReducer());

            // Creating the Tasks. Each partition getting a Task with the exact Trade IDs to calculate
            for (double rate : rates) {
                long startTime = System.currentTimeMillis();
                for (int i = 0; i < workers; i++) {
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
                        logger.log("\nTime to calculate Net present value for "
                                + getMaxTrades() + " Trades using " + rate + " % rate:" + (endTime - startTime) + " ms");
                        for (String key : positions.keySet()) {
                            logger.log("Book = %s, NPV = %3.2f", key, positions.get(key));
                        }
                        Thread.sleep(1000);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }
}
