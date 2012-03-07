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

/**
 * Collector to get statistics on a GigaSpaces platform.
 * 
 * @author luc boutier
 */
public class GigaSpacesPlatformCollector extends Collector {
	private static final Log LOGGER = LogFactory.getLog(GigaSpacesPlatformCollector.class);

	private static final int DISCOVERY_COLLECTION_INTERVAL = 10;
	private int collectionWithoutDiscovery = 0;

	@Override
	public void collect() {
		Properties props = getProperties();

		String locators = props.getProperty(GigaSpacesServicesDetector.LOOKUP_LOCATORS);
		String groups = props.getProperty(GigaSpacesServicesDetector.LOOKUP_GROUPS);
		String user = props.getProperty(GigaSpacesServicesDetector.GS_MONITOR_USER);
		String password = props.getProperty(GigaSpacesServicesDetector.GS_MONITOR_PASSWORD);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Collecting metrics for locators <" + locators + "> groups <" + groups + ">");
		}

		if (locators == null && groups == null) {
			LOGGER.warn("Groups or locators are required. Please set up the GigaSpaces platform.");
			return;
		}

		try {
			if (collectionWithoutDiscovery >= DISCOVERY_COLLECTION_INTERVAL) {
				LOGGER.info("discovery");
				GigaSpacesPlatformDetector.runAutoDiscovery(locators, groups, user, password);
				collectionWithoutDiscovery = 0;
			} else {
				collectionWithoutDiscovery++;
			}

			Admin admin = AdminAPIConnectionManager.getAdmin(locators, groups, user, password);
			admin.getGridServiceAgents().waitForAtLeastOne(10, TimeUnit.SECONDS);
			admin.getGridServiceManagers().waitForAtLeastOne(10, TimeUnit.SECONDS);
			if (admin.getLookupServices().getSize() > 0) {
				setAvailability(true);
			} else {
				setAvailability(false);
			}
			setValue("gsa_count", new Double(admin.getGridServiceAgents().getSize()).doubleValue());
			setValue("lus_count", new Double(admin.getLookupServices().getSize()).doubleValue());
			setValue("gsm_count", new Double(admin.getGridServiceManagers().getSize()).doubleValue());
			setValue("gsc_count", new Double(admin.getGridServiceContainers().getSize()).doubleValue());
		} catch (Exception ex) {
			setAvailability(false);
			LOGGER.warn("error while getting gs metric: " + ex.getMessage(), ex);
		}
	}
}