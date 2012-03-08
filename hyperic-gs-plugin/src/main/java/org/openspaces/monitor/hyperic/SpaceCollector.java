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
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceStatistics;

/**
 * A collector for GSA statistics.
 * 
 * @author luc boutier
 */
public class SpaceCollector extends Collector {
	private static final Log LOGGER = LogFactory.getLog(SpaceCollector.class);

	@Override
	public void collect() {
		Properties props = getProperties();

		String locators = props.getProperty(GigaSpacesServicesDetector.LOOKUP_LOCATORS);
		String groups = props.getProperty(GigaSpacesServicesDetector.LOOKUP_GROUPS);
		String user = props.getProperty(GigaSpacesServicesDetector.GS_MONITOR_USER);
		String password = props.getProperty(GigaSpacesServicesDetector.GS_MONITOR_PASSWORD);

		String spaceName = props.getProperty(GigaSpacesServicesDetector.SPACE_NAME);

		try {
			Admin admin = AdminAPIConnectionManager.getAdmin(locators, groups, user, password);
			Space space = admin.getSpaces().waitFor(spaceName, 5, TimeUnit.SECONDS);

			if (space == null) {
				setAvailability(false);
			} else {
				setAvailability(true);
				
				SpaceStatistics spaceStats = space.getStatistics();

				setValue("nb_instances", space.getNumberOfInstances());
				setValue("nb_backups", space.getNumberOfBackups());
				setValue("nb_read", spaceStats.getReadCount());
				setValue("nb_write", spaceStats.getWriteCount());
				setValue("nb_take", spaceStats.getTakeCount());
				setValue("nb_update", spaceStats.getUpdateCount());
				setValue("nb_execute", spaceStats.getExecuteCount());
				setValue("nb_remove", spaceStats.getRemoveCount());
			}
		} catch (Exception e) {
			setAvailability(false);
			LOGGER.warn("Error while trying to collect metric", e);
		}

	}
}