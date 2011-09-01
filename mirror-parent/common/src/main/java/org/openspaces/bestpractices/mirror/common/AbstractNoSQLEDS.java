package org.openspaces.bestpractices.mirror.common;

import com.gigaspaces.datasource.*;
import com.j_spaces.core.IGSEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractNoSQLEDS implements BulkDataPersister, ManagedDataSource, DisposableBean, InitializingBean {
    protected final Map<String, String> keyPrefixes = new ConcurrentHashMap<String, String>();
    protected final Pattern keyPattern = Pattern.compile("(.+):(.+)");
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected Properties properties = new Properties();

    public String getTypeFromKey(String id) throws InvalidKeyFormatException {
        Matcher matcher = keyPattern.matcher(id);
        if (matcher.matches()) {
            return matcher.group(1).replaceAll("_", ".");
        }
        throw new InvalidKeyFormatException("Type Not Found in id: " + id);
    }

    public String getIdFromKey(String id) throws InvalidKeyFormatException {
        Matcher matcher = keyPattern.matcher(id);
        if (matcher.matches()) {
            return matcher.group(2);
        }
        throw new InvalidKeyFormatException("Type Not Found in id:" + id);
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
    public void executeBulk(List<BulkItem> bulkItems) throws DataSourceException {
        log.debug("entering executeBulk()");
        if (isConnected()) {
            // we assume a (very) small context, normally
            Map<String, Object> context = new HashMap<String, Object>(5);

            preExecute(context);

            for (BulkItem bulkItem : bulkItems) {
                IGSEntry object = (IGSEntry) bulkItem.getItem();

                switch (bulkItem.getOperation()) {
                    case BulkItem.WRITE:
                        System.out.println("BulkItem.WRITE called for " + object);
                        write(context, bulkItem);
                        break;
                    case BulkItem.UPDATE:
                    case BulkItem.PARTIAL_UPDATE:
                        System.out.println("BulkItem.UPDATE called for " + object);
                        update(context, bulkItem);
                        break;
                    case BulkItem.REMOVE:
                        System.out.println("BulkItem.REMOVE called for " + object);
                        remove(context, bulkItem);
                        break;
                    default:
                        System.out.println("unknown operation type " + bulkItem);
                        break;
                }
                postExecute(context);
            }
        } else {
            log.error("not connected");
        }
    }

    protected void postExecute(Map<String, Object> context) {
        // no-op
    }

    protected void preExecute(Map<String, Object> context) {
        // no-op
    }

    protected boolean isConnected() {
        log.error("isConnected() not implemented");
        throw new NotImplementedException();
    }

    protected abstract void update(Map<String, Object> context, BulkItem bulkItem);

    protected abstract void remove(Map<String, Object> context, BulkItem bulkItem);

    protected abstract void write(Map<String, Object> context, BulkItem bulkItem);

    @Override
    public void destroy() throws Exception {
        log.debug("destroy()");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug("afterPropertiesSet()");
    }

    @Override
    public void shutdown() throws DataSourceException {
        log.debug("shutdown()");
    }

    @Override
    public DataIterator initialLoad() throws DataSourceException {
        log.error("initialLoad() not implemented, returning no-op");

        return null;
    }

    /**
     * Sets the property set. Overrides any pre-existing properties.
     *
     * @param properties The new properties
     * @throws DataSourceException not thrown in THIS method.
     */
    @Override
    public void init(Properties properties) throws DataSourceException {
        log.debug("init");
        this.properties = properties;
    }
}
