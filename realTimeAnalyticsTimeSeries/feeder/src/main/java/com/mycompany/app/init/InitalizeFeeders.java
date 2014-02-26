package com.mycompany.app.init;

import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.standalone.StandaloneProcessingUnitContainerProvider;

public class InitalizeFeeders {

	public static void main(String[] args) throws Exception {
		StandaloneProcessingUnitContainerProvider provider = new StandaloneProcessingUnitContainerProvider("target/my-app-feeder.jar");

		//Provide cluster information for the specific PU instance
		ClusterInfo clusterInfo = new ClusterInfo();
		clusterInfo.setSchema("partitioned-sync2backup");
		clusterInfo.setNumberOfInstances(2);
		clusterInfo.setNumberOfBackups(1);
		clusterInfo.setInstanceId(1);
		provider.setClusterInfo(clusterInfo);

		//Build the Spring application context and "start" it
		ProcessingUnitContainer container = provider.createContainer();
		
		//Stop the Feeder after 20 minutes in case the demonstrator forget to manually stop the process
		try {
			Thread.sleep(1200000);
			container.close();
		}catch(Exception e) {
			System.out.println(e);
		}
	}
	
}