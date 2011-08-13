package org.openspaces.ece.client.clients;

import org.openspaces.calcengine.common.CalculateNPVUtil;
import org.openspaces.calcengine.masterworker.Request;
import org.openspaces.calcengine.masterworker.Result;
import org.openspaces.core.GigaSpace;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class ECEMasterWorkerClient extends AbstractECEClient {
    double rates[] = {2, 3, 4, 5, 6, 7, 8};
    public ECEMasterWorkerClient() {
    }

    public ECEMasterWorkerClient(int maxTrades) {
        setMaxTrades(maxTrades);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public ECEMasterWorkerClient(int maxTrades, int maxIterations) {
        this(maxTrades);
        setMaxIterations(maxIterations);
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
                HashMap<Integer, HashSet<Integer>> partitionIDSDistro = CalculateNPVUtil.splitIDs(ids, workers);
                long startTime = System.currentTimeMillis();
                logger.log("--> Executing Job %d with %d workers", i, workers);
                if (execute(i, partitionIDSDistro, rate)) {
                    reduce(i, partitionIDSDistro.size());
                }
                long endTime = System.currentTimeMillis();
                logger.log("--> Time to Execute Job %d - %d ms" , i, endTime-startTime);
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
                for (Result result : results) {
                    HashMap<String, Double> incPositions = result.getResultData();
                    CalculateNPVUtil.subreducer(aggregatedNPVCalc, incPositions);
                }
            }
            // Do we have all the results?
            if (count == totalResultsExpected) {
                logger.log("--> Done executing Job %d", jobId);
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
                logger.log("Book = %s, NPV = %3.2f", key, aggregatedNPVCalc.get(key));
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
                requests[i].setRouting(i % workers);
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
            if (status!=null && !status.isCompleted())
                ptm.rollback(status);
        }
        return false;
    }
}
