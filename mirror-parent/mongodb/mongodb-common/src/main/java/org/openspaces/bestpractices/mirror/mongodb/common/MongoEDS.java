package org.openspaces.bestpractices.mirror.mongodb.common;

import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.document.SpaceDocument;
import com.j_spaces.core.IGSEntry;
import com.mongodb.*;
import org.openspaces.bestpractices.mirror.common.AbstractNoSQLEDS;
import org.openspaces.bestpractices.mirror.common.InvalidKeyFormatException;
import org.springframework.data.document.mongodb.MongoOperations;
import org.springframework.data.document.mongodb.MongoTemplate;

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
        WriteResult s = collection.insert(dbObject);
        if (log.isDebugEnabled()) {
            log.debug("WriteResult: " + s);
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
        WriteResult s = collection.update(query, dbObject);
        if (log.isDebugEnabled()) {
            log.debug("UpdateResult: " + s);
        }
    }

    protected void remove(Map<String, Object> context, BulkItem item) {
        if (log.isDebugEnabled()) {
            log.debug("MongoEDS.executeBulk.remove " + item);
        }
        BasicDBObject queryObject = new BasicDBObject();
        queryObject.put("_id", getKeyValue((IGSEntry) item.getItem()));
        WriteResult s = collection.remove(queryObject);
        if (log.isDebugEnabled()) {
            log.debug("Write result: " + s);
        }
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
                SpaceDocument document = null;
                try {
                    String uid = (String) dbObject.get("_id");
                    String type = getTypeFromKey(uid);
                    String id = getIdFromKey(uid);
                    document = new SpaceDocument(type);

                    for (String key : dbObject.keySet()) {
                        if (!key.startsWith("_")) {
                            document.setProperty(key, dbObject.get(key));
                        }
                    }

                    document.setProperty("id", id);
                } catch (InvalidKeyFormatException ignored) {
                }
                return document;
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
