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
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.product.Collector;
import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;

/**
 * A collector for GSA statistics.
 * 
 * @author luc boutier
 */
public class PUCollector extends Collector {
	private static final Log LOGGER = LogFactory.getLog(PUCollector.class);

	@Override
	public void collect() {
		Properties props = getProperties();

		String locators = props.getProperty(GigaSpacesServicesDetector.LOOKUP_LOCATORS);
		String groups = props.getProperty(GigaSpacesServicesDetector.LOOKUP_GROUPS);
		String user = props.getProperty(GigaSpacesServicesDetector.GS_MONITOR_USER);
		String password = props.getProperty(GigaSpacesServicesDetector.GS_MONITOR_PASSWORD);

		String processingUnitName = props.getProperty(GigaSpacesServicesDetector.PU_NAME);

		try {
			Admin admin = AdminAPIConnectionManager.getAdmin(locators, groups, user, password);
			ProcessingUnit pu = admin.getProcessingUnits().waitFor(processingUnitName, 5, TimeUnit.SECONDS);

			if (pu == null) {
				setAvailability(false);
			} else {
				setAvailability(true);

				if (pu.getStatus().equals(DeploymentStatus.NA) || pu.getStatus().equals(DeploymentStatus.UNDEPLOYED)) {
					setValue("status", 0);
				}
				if (pu.getStatus().equals(DeploymentStatus.SCHEDULED)) {
					setValue("status", 1);
				}
				if (pu.getStatus().equals(DeploymentStatus.DEPLOYED)) {
					setValue("status", 2);
				}
				if (pu.getStatus().equals(DeploymentStatus.BROKEN)) {
					setValue("status", 3);
				}
				if (pu.getStatus().equals(DeploymentStatus.COMPROMISED)) {
					setValue("status", 4);
				}
				if (pu.getStatus().equals(DeploymentStatus.INTACT)) {
					setValue("status", 5);
				}
				setValue("nb_instances", pu.getInstances().length);
			}
		} catch (Exception e) {
			setAvailability(false);
			LOGGER.warn("Error while trying to collect metric", e);
		}

	}
}