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

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;

/**
 * Manages admin API connections to keep only the required ones opened.
 * 
 * @author luc boutier
 */
public class AdminAPIConnectionManager {
	/** Admin API connections. */
	private static Map<String, Admin> adminConnections = new HashMap<String, Admin>();

	/**
	 * Get an admin for the given locator.
	 * 
	 * @param locators
	 *            The gigaspaces locators (hostname:port) format
	 * @return The admin for the given locator.
	 */
	public static Admin getAdmin(String locators, String groups, String user, String password) {
		String adminId = getSignature(locators, groups, user, password);
		Admin admin = adminConnections.get(adminId);
		if (admin == null) {
			AdminFactory af = new AdminFactory();
			if (locators != null) {
				af.addLocators(locators);
			}
			if (groups != null) {
				af.addGroups(groups);
			}
			if (user != null && password != null) {
				af.userDetails(user, password);
			}
			admin = af.create();
			adminConnections.put(adminId, admin);
		}
		return admin;
	}

	public static String getSignature(String locators, String groups, String user, String password) {
		StringBuilder adminIdBuilder = new StringBuilder();
		if (locators != null) {
			adminIdBuilder.append(locators).append("_");
		}
		if (groups != null) {
			adminIdBuilder.append(groups).append("_");
		}
		if (user != null && password != null) {
			adminIdBuilder.append(user).append("_").append(password);
		}
		return adminIdBuilder.toString();
	}
}