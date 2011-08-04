package org.openspaces.calcengine.common;

import com.gigaspaces.client.ReadByIdsResult;
import org.openspaces.core.GigaSpace;

import java.util.*;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Logger;

public class CalculateNPVUtil {
    static Logger logger = Logger.getLogger(CalculateNPVUtil.class.getName());
    static Random random = new Random();

    //calculate Net present value for the last 6 years - http://en.wikipedia.org/wiki/Net_present_value
    static public void calculateNPV(double rate, Trade trade) {
        double disc = 1.0 / (1.0 + rate / 100);
        CashFlowData cf = trade.getCashFlowData();
        double NPV = (cf.getCashFlowYear0() +
                disc * (cf.getCashFlowYear1() +
                        disc * (cf.getCashFlowYear2() +
                                disc * (cf.getCashFlowYear3() +
                                        disc * (cf.getCashFlowYear4() +
                                                disc * cf.getCashFlowYear5())))));
        trade.setNPV(NPV);
    }

    static public void runAnalysis(List<Trade> trades, double rate) {
        for (Trade t : trades) {
            calculateNPV(rate, t);
        }
    }

    static public Trade[] getTradesFromDB(ArrayList<Integer> missingIDs, GigaSpace gigaspace) {
        Trade[] trades = new Trade[missingIDs.size()];
        int i = 0;
        for (Integer id : missingIDs) {
            trades[i] = generateTrade(id);
            i++;
            LockSupport.parkNanos(10 * 1000000); // 1 milli = 1000000 nanoseconds.
        }
        gigaspace.writeMultiple(trades);
        return trades;
    }

    static public HashMap<String, Double> execute(GigaSpace tradeDataSpaceCache, GigaSpace tradeDataSpace, Integer tradeIds[], int partitionID, double rate) throws Exception {

        HashMap<String, Double> rtnVal = new HashMap<String, Double>();

        List<Trade> tradesList = new ArrayList<Trade>();
        try {
            ReadByIdsResult<Trade> res = tradeDataSpaceCache.readByIds(Trade.class, tradeIds);

            // checking for null results and getting missing Trades objects from external Data source
            Trade resArr[] = res.getResultsArray();
            ArrayList<Integer> missingIDs = new ArrayList<Integer>();
            for (int i = 0; i < resArr.length; i++) {
                if (resArr[i] == null) {
                    missingIDs.add(tradeIds[i]);
                }
            }
            Trade missingTrades[] = null;
            if (missingIDs.size() > 0) {
                logger.fine(">>>> Partition:" + partitionID + " - Loading missing Trades from the database for IDs:" + missingIDs);
                missingTrades = getTradesFromDB(missingIDs, tradeDataSpace);
            }

            // this ignores null results, or else we'd use addAll?
            for (Trade trade : res) {
                tradesList.add(trade);
            }

            if (missingTrades != null) {
                Collections.addAll(tradesList, missingTrades);
            }
        } catch (Exception e) {
            String a = e.getMessage();
            System.out.println(a);
            e.printStackTrace();
        }
        runAnalysis(tradesList, rate);

        for (Trade t : tradesList) {
            String key = t.getBook();
            if (rtnVal.containsKey(key)) {
                rtnVal.put(key, rtnVal.get(key) + t.getNPV());
            } else {
                rtnVal.put(key, t.getNPV());
            }
        }
        return rtnVal;
    }


    // creating a trade
    static public Trade generateTrade(int id) {
        Trade trade = new Trade();
        trade.setId(id);
        CashFlowData cf = new CashFlowData();
        cf.setCashFlowYear0(id * -100 * (random.nextDouble() + 0.5));
        cf.setCashFlowYear1(id * 20 * (random.nextDouble() + 0.5));
        cf.setCashFlowYear2(id * 40 * (random.nextDouble() + 0.5));
        cf.setCashFlowYear3(id * 60 * (random.nextDouble() + 0.5));
        cf.setCashFlowYear4(id * 80 * (random.nextDouble() + 0.5));
        cf.setCashFlowYear5(id * 100 * (random.nextDouble() + 0.5));
        trade.setCashFlowData(cf);
        return trade;
    }

    // splitting the IDs into chunks
    static public HashMap<Integer, HashSet<Integer>> splitIDs(Integer[] ids, int partitionCount) {
        HashMap<Integer, HashSet<Integer>> routings = new HashMap<Integer, HashSet<Integer>>();
        for (Integer id : ids) {
            int partition = (partitionCount == 1) ? id : (id % partitionCount);
            HashSet<Integer> partitionIDS = null;
            if (routings.containsKey(partition)) {
                partitionIDS = routings.get(partition);
            } else {
                partitionIDS = new HashSet<Integer>();
            }
            partitionIDS.add(id);
            routings.put(partition, partitionIDS);
        }
        return routings;
    }

    static public void subreducer(Map<String, Double> aggregatedNPVCalc, Map<String, Double> incPositions) {
        for (String key : incPositions.keySet()) {
            if (aggregatedNPVCalc.containsKey(key)) {
                double currentNPV = aggregatedNPVCalc.get(key);
                aggregatedNPVCalc.put(key, currentNPV + incPositions.get(key));
            } else {
                aggregatedNPVCalc.put(key, incPositions.get(key));
            }
        }
    }

}
