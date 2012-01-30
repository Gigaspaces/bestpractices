package org.openspaces.calcengine.masterworker;

import java.util.HashMap;

import com.gigaspaces.annotation.pojo.SpaceClass;

@SpaceClass
public class Result extends Base {
	public Result (){}

	HashMap<String, Double> resultData;

	public HashMap<String, Double> getResultData() {
		return resultData;
	}

	public void setResultData(HashMap<String, Double> resultData) {
		this.resultData = resultData;
	}
	
}
