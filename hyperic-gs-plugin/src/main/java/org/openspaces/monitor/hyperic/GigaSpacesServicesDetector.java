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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.product.AutoServerDetector;
import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.product.ServerDetector;
import org.hyperic.hq.product.ServerResource;
import org.hyperic.hq.product.ServiceResource;
import org.hyperic.util.config.ConfigResponse;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.lus.LookupServices;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.zone.Zone;

/**
 * Connect to admin API and detect the lookup services.
 * 
 * @author luc boutier
 */
public class GigaSpacesServicesDetector extends ServerDetector implements AutoServerDetector {
	private static final Log LOGGER = LogFactory.getLog(GigaSpacesServicesDetector.class);

	public static final String LOOKUP_LOCATORS = "connection.locators";
	public static final String LOOKUP_GROUPS = "connection.groups";
	public static final String GS_MONITOR_USER = "connection.user";
	public static final String GS_MONITOR_PASSWORD = "connection.password";
	public static final String GRID_ELEMENT_TYPE = "grid.type";
	public static final String GRID_ELEMENT_HOST = "grid.host";
	public static final String GRID_ELEMENT_AGENT_ID = "grid.agentId";
	public static final String PU_NAME = "pu.name";
	public static final String SPACE_NAME = "space.name";

	public List<ServerResource> getServerResources(ConfigResponse platformConfig) throws PluginException {
		String locators = platformConfig.getValue(LOOKUP_LOCATORS);
		String groups = platformConfig.getValue(LOOKUP_GROUPS);
		String user = platformConfig.getValue(GS_MONITOR_USER);
		String password = platformConfig.getValue(GS_MONITOR_PASSWORD);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("getServerResources locators <" + locators + "> groups <" + groups + ">");
		}

		Admin admin;
		if (locators == null && groups == null) {
			return null;
		} else {
			admin = AdminAPIConnectionManager.getAdmin(locators, groups, user, password);
		}

		LookupServices lookupServices = admin.getLookupServices();

		if (lookupServices.getSize() == 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOGGER.warn("Thread interrupted..", e);
			}
		}

		LookupService[] services = lookupServices.getLookupServices();
		List<ServerResource> serverRessources = new ArrayList<ServerResource>();
		for (LookupService lus : services) {
			ServerResource serverResource = createServerResource("");
			serverResource.setType(getTypeInfo().getName());

			StringBuilder idBuilder = new StringBuilder("GigaSpaces_XAP_");
			StringBuilder nameBuilder = new StringBuilder(getTypeInfo().getName()).append(" <");

			ConfigResponse config = new ConfigResponse();

			if (groups != null) {
				idBuilder.append(groups);
				nameBuilder.append(groups);
				config.setValue(LOOKUP_GROUPS, groups);
			}

			if (locators != null) {
				if (groups != null) {
					idBuilder.append("_");
					nameBuilder.append("_");
				}
				idBuilder.append(locators);
				nameBuilder.append(locators);
				config.setValue(LOOKUP_LOCATORS, locators);
			}

			if (locators == null && groups == null) {
				idBuilder.append(lus.getLookupLocator().getHost() + ":" + lus.getLookupLocator().getPort());
				nameBuilder.append(lus.getLookupLocator().getHost() + ":" + lus.getLookupLocator().getPort());
				config.setValue(LOOKUP_LOCATORS, lus.getLookupLocator().getHost() + ":"
						+ lus.getLookupLocator().getPort());
			}

			if (user != null) {
				config.setValue(GS_MONITOR_USER, user);
			}
			if (password != null) {
				config.setValue(GS_MONITOR_PASSWORD, password);
			}

			nameBuilder.append(">");

			serverResource.setIdentifier(idBuilder.toString());
			serverResource.setName(nameBuilder.toString());
			serverResource.setConnectProperties(new String[] { LOOKUP_LOCATORS, LOOKUP_GROUPS, GS_MONITOR_USER,
					GS_MONITOR_PASSWORD });

			setProductConfig(serverResource, new ConfigResponse());
			setMeasurementConfig(serverResource, config);

			serverRessources.add(serverResource);

			LOGGER.info("discovered server XAP " + serverResource.getName());

			if (locators != null || groups != null) {
				return serverRessources;
			}
		}

		return serverRessources;
	}

	@Override
	protected List<ServiceResource> discoverServices(ConfigResponse pc) throws PluginException {
		LOGGER.info("discoverServices <" + pc + ">");

		String locators = pc.getValue(LOOKUP_LOCATORS);
		String groups = pc.getValue(LOOKUP_GROUPS);
		String user = pc.getValue(GS_MONITOR_USER);
		String password = pc.getValue(GS_MONITOR_PASSWORD);

		List<ServiceResource> serviceResources = new ArrayList<ServiceResource>();

		Admin admin = AdminAPIConnectionManager.getAdmin(locators, groups, user, password);
		admin.getGridServiceAgents().waitForAtLeastOne(10, TimeUnit.SECONDS);
		for (GridServiceAgent gsa : admin.getGridServiceAgents().getAgents()) {
			ServiceResource sr = createServiceResource("GSA");
			sr.setName("GSA_" + gsa.getMachine().getHostAddress());

			ConfigResponse c = new ConfigResponse();
			c.setValue(GRID_ELEMENT_TYPE, "GSA");
			c.setValue(GRID_ELEMENT_HOST, gsa.getMachine().getHostAddress());

			boolean firstZone = true;
			StringBuilder zonesBuilder = new StringBuilder("");
			Map<String, Zone> zones = gsa.getZones();
			if (zones != null) {
				for (Zone zone : zones.values()) {
					if (firstZone) {
						zonesBuilder.append(zone.getName());
						firstZone = false;
					} else {
						zonesBuilder.append(",").append(zone.getName());
					}
				}
			}

			ConfigResponse attr = new ConfigResponse();
			attr.setValue("zones", zonesBuilder.toString());

			setMeasurementConfig(sr, c);
			sr.setCustomProperties(attr);
			serviceResources.add(sr);
		}

		for (GridServiceManager gsm : admin.getGridServiceManagers().getManagers()) {
			ServiceResource sr = createServiceResource("GSM");
			sr.setName("GSM_" + gsm.getMachine().getHostAddress());

			ConfigResponse c = new ConfigResponse();
			c.setValue(GRID_ELEMENT_TYPE, "GSM");
			c.setValue(GRID_ELEMENT_HOST, gsm.getMachine().getHostAddress());

			boolean firstZone = true;
			StringBuilder zonesBuilder = new StringBuilder("");
			Map<String, Zone> zones = gsm.getZones();
			if (zones != null) {
				for (Zone zone : zones.values()) {
					if (firstZone) {
						zonesBuilder.append(zone.getName());
						firstZone = false;
					} else {
						zonesBuilder.append(",").append(zone.getName());
					}
				}
			}

			ConfigResponse attr = new ConfigResponse();
			attr.setValue("zones", zonesBuilder.toString());

			setMeasurementConfig(sr, c);
			sr.setCustomProperties(attr);
			serviceResources.add(sr);
		}

		for (GridServiceContainer gsc : admin.getGridServiceContainers().getContainers()) {
			ServiceResource sr = createServiceResource("GSC");
			sr.setName("GSC_" + gsc.getMachine().getHostAddress() + "_" + gsc.getAgentId());

			ConfigResponse c = new ConfigResponse();
			c.setValue(GRID_ELEMENT_TYPE, "GSC");
			c.setValue(GRID_ELEMENT_HOST, gsc.getMachine().getHostAddress());
			c.setValue(GRID_ELEMENT_AGENT_ID, gsc.getAgentId());

			boolean firstZone = true;
			StringBuilder zonesBuilder = new StringBuilder("");
			Map<String, Zone> zones = gsc.getZones();
			if (zones != null) {
				for (Zone zone : zones.values()) {
					if (firstZone) {
						zonesBuilder.append(zone.getName());
						firstZone = false;
					} else {
						zonesBuilder.append(",").append(zone.getName());
					}
				}
			}

			ConfigResponse attr = new ConfigResponse();
			attr.setValue("zones", zonesBuilder.toString());

			setMeasurementConfig(sr, c);
			sr.setCustomProperties(attr);
			serviceResources.add(sr);
		}

		for (ProcessingUnit pu : admin.getProcessingUnits().getProcessingUnits()) {
			ServiceResource sr = createServiceResource("PU");
			sr.setName("PU_" + pu.getName());

			ConfigResponse c = new ConfigResponse();
			c.setValue(PU_NAME, pu.getName());

			setMeasurementConfig(sr, c);
			serviceResources.add(sr);
		}
		
		for (Space space : admin.getSpaces().getSpaces()) {
			ServiceResource sr = createServiceResource("SPACE");
			sr.setName("SPACE_" + space.getName());

			ConfigResponse c = new ConfigResponse();
			c.setValue(SPACE_NAME, space.getName());

			setMeasurementConfig(sr, c);
			serviceResources.add(sr);
		}
		
		return serviceResources;
	}
}