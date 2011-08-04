package org.openspaces.ece.client.executors;

import com.gigaspaces.annotation.pojo.SpaceRouting;
import com.j_spaces.core.client.SpaceURL;
import org.openspaces.calcengine.common.CalculateNPVUtil;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.Task;
import org.openspaces.core.executor.TaskGigaSpace;

import java.util.HashMap;

public class AnalysisTask implements Task<HashMap<String, Double>> {

    @TaskGigaSpace
    transient GigaSpace gigaspace;
    Integer[] tradeIds;
    Integer routing;
    Double rate;

    public AnalysisTask(Integer[] tradeIds, Integer routing, Double rate) {
        this.tradeIds = tradeIds;
        this.routing = routing;
        this.rate = rate;
    }

    public HashMap<String, Double> execute() throws Exception {
        int partitionID = Integer.valueOf(gigaspace.getSpace().getURL().getProperty(SpaceURL.CLUSTER_MEMBER_ID)).intValue() - 1;
        System.out.println("Time: " + System.currentTimeMillis() + " Execute on Partition ID: " + partitionID + " Routing ID:" + routing);
        return CalculateNPVUtil.execute(gigaspace, gigaspace, tradeIds, partitionID, rate);
    }


    @SpaceRouting
    public Integer getRouting() {
        return routing;
    }

    public void setRouting(Integer routing) {
        this.routing = routing;
    }

}