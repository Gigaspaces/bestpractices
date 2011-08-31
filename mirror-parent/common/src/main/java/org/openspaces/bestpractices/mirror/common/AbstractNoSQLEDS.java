package org.openspaces.bestpractices.mirror.common;

import com.gigaspaces.datasource.BulkDataPersister;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.datasource.ManagedDataSource;
import com.j_spaces.core.IGSEntry;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractNoSQLEDS implements BulkDataPersister, ManagedDataSource, DisposableBean, InitializingBean {
    protected final Map<String, String> keyPrefixes = new ConcurrentHashMap<String, String>();
    protected final Pattern keyPattern = Pattern.compile("(.+):(.+)");

    public String getTypeFromKey(String id) {
        Matcher matcher = keyPattern.matcher(id);
        if (matcher.matches()) {
            return matcher.group(1).replaceAll("_", ".");
        }
        throw new RuntimeException("Type Not Found in Cassandra row, " + id);
    }

    public String getIdFromKey(String id) {
        Matcher matcher = keyPattern.matcher(id);
        if (matcher.matches()) {
            return matcher.group(2);
        }
        throw new RuntimeException("Type Not Found in Cassandra row, " + id);
    }

    public String getKeyValue(IGSEntry item) {
        String prefix = keyPrefixes.get(item.getClassName());
        if (prefix == null) {
            prefix = item.getClassName().replaceAll("\\.", "_") + ":";
            keyPrefixes.put(item.getClassName(), prefix);
        }
        return prefix + item.getUID();
    }

    @Override
    public void init(Properties properties) throws DataSourceException {
    }
}
