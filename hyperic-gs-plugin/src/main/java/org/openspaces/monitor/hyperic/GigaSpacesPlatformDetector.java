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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.agent.AgentCommand;
import org.hyperic.hq.agent.AgentRemoteValue;
import org.hyperic.hq.agent.server.AgentDaemon;
import org.hyperic.hq.autoinventory.ScanConfigurationCore;
import org.hyperic.hq.autoinventory.agent.AICommandsAPI;
import org.hyperic.hq.product.PlatformDetector;
import org.hyperic.hq.product.PlatformResource;
import org.hyperic.hq.product.PluginException;
import org.hyperic.util.config.ConfigResponse;

/**
 * Detect GigaSpaces platforms on the system.
 * 
 * @author luc boutier
 */
public class GigaSpacesPlatformDetector extends PlatformDetector {
	/** The GigaSpaces platform detector. */
	private static final Log LOGGER = LogFactory.getLog(GigaSpacesPlatformDetector.class);

	private static ConfigResponse configResponse;

	@Override
	public PlatformResource getPlatformResource(ConfigResponse config) throws PluginException {
		LOGGER.info("getPlatformResource <" + config + ">");
		PlatformResource res = super.getPlatformResource(config);
		configResponse = config;
		return res;
	}

	public static void runAutoDiscovery(String locators, String groups, String user, String password) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("runAutoDiscovery <" + locators + "> <" + groups + ">");
		}
		try {
			ScanConfigurationCore scanConfig = new ScanConfigurationCore();
			ConfigResponse c;
			if (configResponse != null) {
				c = configResponse;
			} else {
				c = new ConfigResponse();
			}
			c.setValue("platform.name", getPlatformName());
			c.setValue(GigaSpacesServicesDetector.LOOKUP_LOCATORS, locators);
			c.setValue(GigaSpacesServicesDetector.LOOKUP_GROUPS, groups);
			c.setValue(GigaSpacesServicesDetector.GS_MONITOR_USER, user);
			c.setValue(GigaSpacesServicesDetector.GS_MONITOR_PASSWORD, password);

			scanConfig.setConfigResponse(c);
			AgentRemoteValue configARV = new AgentRemoteValue();
			scanConfig.toAgentRemoteValue(AICommandsAPI.PROP_SCANCONFIG, configARV);
			AgentCommand ac = new AgentCommand(1, 1, "autoinv:startScan", configARV);
			AgentDaemon.getMainInstance().getCommandDispatcher().processRequest(ac, null, null);
			LOGGER.debug("runAutoDiscovery ok");
		} catch (Exception ex) {
			LOGGER.error("runAutoDiscovery failed" + ex.getMessage(), ex);
		}
	}
}