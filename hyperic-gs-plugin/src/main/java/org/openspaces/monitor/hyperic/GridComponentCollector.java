/**
 * Copyright 2012 Luc Boutier
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openspaces.monitor.hyperic;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.product.Collector;
import org.openspaces.admin.Admin;
import org.openspaces.admin.GridComponent;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.vm.VirtualMachineStatistics;

/**
 * A collector for GSA statistics.
 * 
 * @author luc boutier
 */
public class GridComponentCollector extends Collector {
	private static final Log LOGGER = LogFactory.getLog(GridComponentCollector.class);

	@Override
	public void collect() {
		Properties props = getProperties();

		String locators = props.getProperty(GigaSpacesServicesDetector.LOOKUP_LOCATORS);
		String groups = props.getProperty(GigaSpacesServicesDetector.LOOKUP_GROUPS);
		String user = props.getProperty(GigaSpacesServicesDetector.GS_MONITOR_USER);
		String password = props.getProperty(GigaSpacesServicesDetector.GS_MONITOR_PASSWORD);

		String componentType = props.getProperty(GigaSpacesServicesDetector.GRID_ELEMENT_TYPE);
		String host = props.getProperty(GigaSpacesServicesDetector.GRID_ELEMENT_HOST);
		String agentId = props.getProperty(GigaSpacesServicesDetector.GRID_ELEMENT_AGENT_ID);

		try {
			Admin admin = AdminAPIConnectionManager.getAdmin(locators, groups, user, password);
			GridComponent gridComponent = null;

			if (componentType.equals("GSA")) {
				GridServiceAgent[] agents = admin.getGridServiceAgents().getAgents();
				for (GridServiceAgent gsa : agents) {
					if (gsa.getMachine().getHostAddress().equals(host)) {
						gridComponent = gsa;
						break;
					}
				}
			}
			if (componentType.equals("GSM")) {
				GridServiceManager[] managers = admin.getGridServiceManagers().getManagers();
				for (GridServiceManager gsm : managers) {
					if (gsm.getMachine().getHostAddress().equals(host)) {
						gridComponent = gsm;
						break;
					}
				}
			}
			if (componentType.equals("GSC")) {
				GridServiceContainer[] containers = admin.getGridServiceContainers().getContainers();
				for (GridServiceContainer gsc : containers) {
					if (gsc.getMachine().getHostAddress().equals(host)
							&& agentId.equals(String.valueOf(gsc.getAgentId()))) {
						gridComponent = gsc;
						break;
					}
				}
			}

			if (gridComponent == null) {
				setAvailability(false);
			} else {
				setAvailability(true);
				VirtualMachineStatistics vms = gridComponent.getVirtualMachine().getStatistics();

				double cpuPerc = vms.getCpuPerc();
				if (cpuPerc >= 0) {
					setValue("used_cpu", vms.getCpuPerc());
				}
				setValue("used_memory", vms.getMemoryHeapUsedPerc() / 100);
			}
		} catch (Exception e) {
			setAvailability(false);
			LOGGER.warn("Error while trying to collect metric", e);
		}

	}
}