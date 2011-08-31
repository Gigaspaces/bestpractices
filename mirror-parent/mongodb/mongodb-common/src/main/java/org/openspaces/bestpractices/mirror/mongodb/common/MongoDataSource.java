package org.openspaces.bestpractices.mirror.mongodb.common;

import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.document.SpaceDocument;
import com.j_spaces.core.IGSEntry;
import com.mongodb.*;
import org.mvel2.MVEL;
import org.openspaces.bestpractices.mirror.common.AbstractNoSQLEDS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.document.mongodb.MongoOperations;
import org.springframework.data.document.mongodb.MongoTemplate;
import org.springframework.data.document.mongodb.query.BasicQuery;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MongoDataSource extends AbstractNoSQLEDS {
    private static Logger log = LoggerFactory.getLogger(MongoDataSource.class);

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

    private boolean isConnected() {
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

    public void shutdown() throws DataSourceException {
    }


    public void executeBulk(List<BulkItem> bulk) throws DataSourceException {
        if (!isConnected()) {
            //throw new DataSourceException("Mongo Database not connected, failed to perform DB operation");
            log.error("Mongo Database not connected, failed to perform DB operation");
            return;
        }
        for (BulkItem bulkItem : bulk) {
            IGSEntry object = (IGSEntry) bulkItem.getItem();

            switch (bulkItem.getOperation()) {
                case BulkItem.WRITE:
                    System.out.println("BulkItem.WRITE called for " + object);
                    executeWrite(bulkItem);
                    break;
                case BulkItem.UPDATE:
                case BulkItem.PARTIAL_UPDATE:
                    System.out.println("BulkItem.UPDATE called for " + object);
                    executeUpdate(bulkItem);
                    break;
                case BulkItem.REMOVE:
                    System.out.println("BulkItem.REMOVE called for " + object);
                    removeDocument(bulkItem);
                    break;
                default:
                    System.out.println("unknown operation type "+bulkItem);
                    break;
            }
        }
    }

    private void executeWrite(BulkItem item) {
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

    private void executeUpdate(BulkItem item) {
        if (log.isDebugEnabled()) {
            log.debug("MongoEDS.executeBulk.update " + item);
        }
        BasicDBObject query = new BasicDBObject();
        String uid = getKeyValue((IGSEntry) item.getItem());
        query.put("_id", uid);
        BasicDBObject dbObject = new BasicDBObject();
        for (String key : item.getItemValues().keySet()) {
            if (item.getItemValues().get(key) != null) {
                dbObject.put(key, item.getItemValues().get(key));
            }
        }
        System.out.println(dbObject);
        WriteResult s = collection.update(query, dbObject);
        if (log.isDebugEnabled()) {
            log.debug("UpdateResult: " + s);
        }
    }

    private void removeDocument(BulkItem item) {
        //if (log.isDebugEnabled()) {
        System.out.println("MongoEDS.executeBulk.remove " + item);
        //}
        BasicDBObject queryObject = new BasicDBObject();
        queryObject.put("_id", item.getIdPropertyValue());
        System.out.println(queryObject);
        WriteResult s = collection.remove(queryObject);
        //if (log.isDebugEnabled()) {
        System.out.println("RemoveResult: " + s);
        //}
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
                Object o = null;
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
