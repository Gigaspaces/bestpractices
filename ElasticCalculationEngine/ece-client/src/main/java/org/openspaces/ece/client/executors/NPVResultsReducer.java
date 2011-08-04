package org.openspaces.ece.client.executors;

import com.gigaspaces.async.AsyncResult;
import com.gigaspaces.async.AsyncResultsReducer;
import org.openspaces.calcengine.common.CalculateNPVUtil;

import java.util.HashMap;
import java.util.List;

public class NPVResultsReducer implements AsyncResultsReducer<HashMap<String, Double>, HashMap<String, Double>>{

	@Override
	// we are getting HashMap<String, Double> from each AnalysisTask execute() 
	public HashMap<String, Double> reduce(List<AsyncResult<HashMap<String, Double>>> result) throws Exception {

		HashMap<String, Double> aggregatedNPVCalc = new HashMap<String, Double>();
		
		for (AsyncResult<HashMap<String, Double>> asyncResult : result) {
			HashMap<String, Double> incPositions = asyncResult.getResult();
			CalculateNPVUtil.subreducer(aggregatedNPVCalc, incPositions);
		}
		return aggregatedNPVCalc ;
	}

}
