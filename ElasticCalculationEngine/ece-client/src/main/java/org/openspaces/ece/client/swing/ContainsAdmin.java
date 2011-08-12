package org.openspaces.ece.client.swing;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;

public interface ContainsAdmin {
	Admin getAdmin();

	GridServiceAgent getGSA();

	void setAdmin(Admin admin);

	void setGSA(GridServiceAgent gridServiceAgent);
}
