package org.openspaces.calcengine.masterworker;

import org.openspaces.calcengine.common.CalculateNPVUtil;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.events.EventDriven;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.TransactionalEvent;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;
import org.openspaces.events.polling.ReceiveHandler;
import org.openspaces.events.polling.receive.ReceiveOperationHandler;
import org.openspaces.events.polling.receive.SingleTakeReceiveOperationHandler;

import java.util.HashMap;
import java.util.logging.Logger;

@EventDriven
@Polling(concurrentConsumers = 1, receiveTimeout = 1000, gigaSpace = "gigaSpace")
@TransactionalEvent
public class Worker implements ClusterInfoAware {
    Logger logger= Logger.getLogger(this.getClass().getName());
    int workerID = 1;

    @GigaSpaceContext(name = "gigaSpace")
    GigaSpace gigaspace;

    @GigaSpaceContext(name = "gigaSpaceLocalCache")
    GigaSpace gigaspaceLocalCache;

    public void setClusterInfo(ClusterInfo clusterInfo) {
        logger.severe("--------------- > setClusterInfo called");
        if (clusterInfo != null) {
            workerID = clusterInfo.getInstanceId();
            logger.severe("--------------- > Worker " + workerID + " started");
        }
    }

    public Worker() {
        logger.severe("-- > Creating Worker");
    }

    @EventTemplate
    Request getTemplate() {
        return new Request();
    }

    @ReceiveHandler
    ReceiveOperationHandler receiveHandler() {
        SingleTakeReceiveOperationHandler receiveHandler = new SingleTakeReceiveOperationHandler();
        receiveHandler.setNonBlocking(true);
        receiveHandler.setNonBlockingFactor(10);
        return receiveHandler;
    }

    @SpaceDataEvent
    public Result execute(Request request) {
        logger.info(" Worker " + workerID + " execute called to calculate " +
                request.getTradeIds().length + " trades");
        Result result = new Result();
        result.setJobID(request.getJobID());
        result.setTaskID(request.getTaskID());
        result.setRouting(request.getRouting());
        try {
            HashMap<String, Double> resultData = CalculateNPVUtil.execute(gigaspaceLocalCache,
                    request.getTradeIds(),
                    workerID,
                    request.getRate());
            result.setResultData(resultData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
