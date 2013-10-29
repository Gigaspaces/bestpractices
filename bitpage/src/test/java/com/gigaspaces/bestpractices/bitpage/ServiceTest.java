package com.gigaspaces.bestpractices.bitpage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;
import org.openspaces.remoting.ExecutorRemotingProxyConfigurer;
import org.openspaces.remoting.RemoteRoutingHandler;
import org.openspaces.remoting.SpaceRemotingInvocation;

import com.gigaspaces.bestpractices.bitpage.BitPage;
import com.gigaspaces.bestpractices.bitpage.service.BitPageService;

public class ServiceTest {
	static final int partitions=3;
	static List<ProcessingUnitContainer> containers=new ArrayList<ProcessingUnitContainer>();
	static GigaSpace space;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		for(int i=0;i<partitions;i++){
			IntegratedProcessingUnitContainerProvider provider = new IntegratedProcessingUnitContainerProvider();
			// provide cluster information for the specific PU instance
			ClusterInfo clusterInfo = new ClusterInfo();
			clusterInfo.setSchema("partitioned-sync2backup");
			clusterInfo.setNumberOfInstances(partitions);
			clusterInfo.setNumberOfBackups(0);
			clusterInfo.setInstanceId(i+1);
			provider.setClusterInfo(clusterInfo);

			// Build the Spring application context and "start" it
			containers.add( provider.createContainer());
		}
		space=((IntegratedProcessingUnitContainer)containers.get(0)).getApplicationContext().getBean(GigaSpace.class).getClustered();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		for(int i=0;i<partitions;i++)
			containers.get(i).close();
	}

	@Before
	public void setUp() throws Exception {
		space.clear(new Object());
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void setTest() {
		BitPageService svc=new ExecutorRemotingProxyConfigurer<BitPageService>(space, BitPageService.class)
			.remoteRoutingHandler(new RemoteRoutingHandler<Integer>(){
				public Integer computeRouting(
						SpaceRemotingInvocation remotingEntry) {
					return new BitPage().getPageId((Integer)remotingEntry.getArguments()[0]);
				}
				
			})
           .proxy();
		
		System.out.println("calling svc with routing="+new BitPage().getPageId(1));
		boolean res=svc.set(1);
		org.junit.Assert.assertFalse(res);
	}

	@Test
	public void getTest() {
		BitPageService svc=new ExecutorRemotingProxyConfigurer<BitPageService>(space, BitPageService.class)
			.remoteRoutingHandler(new RemoteRoutingHandler<Integer>(){
				public Integer computeRouting(
						SpaceRemotingInvocation remotingEntry) {
					return new BitPage().getPageId((Integer)remotingEntry.getArguments()[0]);
				}
				
			})
           .proxy();
		
		System.out.println("calling svc with routing="+new BitPage().getPageId(1));
		boolean res=svc.set(1);
		assertFalse(res);
		
		assertTrue(svc.exists(1));
	}
	@Test
	public void clearTest() {
		BitPageService svc=new ExecutorRemotingProxyConfigurer<BitPageService>(space, BitPageService.class)
			.remoteRoutingHandler(new RemoteRoutingHandler<Integer>(){
				public Integer computeRouting(
						SpaceRemotingInvocation remotingEntry) {
					return new BitPage().getPageId((Integer)remotingEntry.getArguments()[0]);
				}
				
			})
           .proxy();
		
		System.out.println("calling svc with routing="+new BitPage().getPageId(1));
		boolean res=svc.set(1);
		assertFalse(res);
		
		svc.clear(1);
		
		assertFalse(svc.exists(1));
	}
}
