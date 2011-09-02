package org.openspaces.bestpractices.mirror.mongodb.common;

import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.document.SpaceDocument;
import com.j_spaces.core.IGSEntry;
import com.mongodb.*;
import org.mvel2.MVEL;
import org.openspaces.bestpractices.mirror.common.AbstractNoSQLEDS;
import org.openspaces.bestpractices.mirror.common.InvalidKeyFormatException;
import org.springframework.data.document.mongodb.MongoOperations;
import org.springframework.data.document.mongodb.MongoTemplate;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MongoEDS extends AbstractNoSQLEDS {
    Mongo mongo;
    String databaseName;
    MongoOperations mongoOperations;
    DBCollection collection;

    public void setMongo(Mongo mongo) {
        this.mongo = mongo;
        collection = mongo.getDB(databaseName).getCollection("IGSEntry");
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void afterPropertiesSet() {
        mongoOperations = new MongoTemplate(mongo, databaseName);
    }

    @Override
    protected boolean isConnected() {
        if (mongo == null)
            return false;
        List<String> dbs;
        try {
            dbs = mongo.getDatabaseNames();
        } catch (Exception e) {
            log.error("Exception on getDatabaseNames, may be disconnected {}", e.getMessage());
            mongo = null;
            return false;
        }
        return true;
    }

    public void init(Properties props) throws DataSourceException {
    }

    @Override
    public void executeBulk(List<BulkItem> bulkItems) throws DataSourceException {
        for(BulkItem item:bulkItems) {
            System.out.println(item.getItem());
        }
        super.executeBulk(bulkItems);
    }

    public void shutdown() throws DataSourceException {
    }

    protected void write(Map<String, Object> context, BulkItem item) {
        if (log.isDebugEnabled()) {
            log.debug("MongoEDS.executeBulk.write " + item);
        }
        String uid = getKeyValue((IGSEntry) item.getItem());
        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put("_id", uid);
        for (String key : item.getItemValues().keySet()) {
            if (item.getItemValues().get(key) != null) {
                dbObject.put(key, item.getItemValues().get(key));
            }
        }
        System.out.println(dbObject);
        WriteResult s = collection.insert(dbObject);
        if (log.isDebugEnabled()) {
            log.debug("WriteResult: " + s);
        }
    }

    private String extractCollectionName(Object object) {
        if (object instanceof SpaceDocument) {
            return ((SpaceDocument) object).getTypeName();
        } else {
            return StringUtils.uncapitalize(object.getClass().getSimpleName());
        }
    }

    protected void update(Map<String, Object> context, BulkItem item) {
        if (log.isDebugEnabled()) {
            log.debug("MongoEDS.executeBulk.update " + item);
        }
        BasicDBObject query = new BasicDBObject();
        String uid = getKeyValue((IGSEntry) item.getItem());
        query.put("_id", uid);
        BasicDBObject dbObject = new BasicDBObject();
        for (String key : item.getItemValues().keySet()) {
            if (item.getItemValues().get(key) != null) {
                if (item.getItemValues().get(key) instanceof Map) {
                } else {
                    dbObject.put(key, item.getItemValues().get(key));
                }
            }
        }
        System.out.println(dbObject);
        WriteResult s = collection.update(query, dbObject);
        if (log.isDebugEnabled()) {
            log.debug("UpdateResult: " + s);
        }
    }

    protected void remove(Map<String, Object> context, BulkItem item) {
        BasicDBObject queryObject = new BasicDBObject();
        queryObject.put("_id", item.getIdPropertyValue());
        WriteResult s = collection.remove(queryObject);

    }

    public DataIterator initialLoad() throws DataSourceException {
        return new DataIterator() {
            DBCursor cursor;

            {
                cursor = collection.find(new BasicDBObject());
            }

            @Override
            public void close() {
            }

            @Override
            public boolean hasNext() {
                return cursor.hasNext();
            }

            @Override
            public Object next() {
                DBObject dbObject = cursor.next();
                log.error(dbObject.toString());
                Object o = null;
                try {
                    String uid = (String) dbObject.get("_id");
                    String type = getTypeFromKey(uid);
                    String id = getIdFromKey(uid);
                    try {
                        o = Class.forName(type).newInstance();
                        Map<String, Object> context = new HashMap<String, Object>();
                        context.put("o", o);
                        for (String key : dbObject.keySet()) {
                            if (!key.startsWith("_")) {
                                String expression = "o." + key + "=\"" + dbObject.get(key) + "\"";
                                MVEL.eval(expression, context);
                            }
                        }
                        MVEL.eval("o.id=\"" + uid + "\"", context);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } catch (InvalidKeyFormatException e) {
                }
                return o;
            }

            @Override
            public void remove() {
                cursor.remove();
            }
        };
    }

    @Override
    public void destroy() throws Exception {
    }
}
