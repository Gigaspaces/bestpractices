package org.openspaces.ece.client;

import java.io.File;
import java.util.Map;

public interface ContainsResources {
	Map<String, File> getResources();

	void setResource(String key, File value);
}
